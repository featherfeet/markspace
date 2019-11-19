package controllers;

/** @file RenderQuestionController.java
 * Controller for /render_question.
 * @see controllers.RenderQuestionController
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import spark.*;
import storage.PersistentStorage;
import storage.Test;
import storage.TestFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Controller to handle GET requests to /render_question.
 */
public class RenderQuestionController extends Controller {
    /**
     * pdfRendererCache is used to share PDFRenderer objects between multiple controllers and request handlers.
     * Each PDFRenderer is given a unique key of the format "user_id:test_id:answers" where user_id is the user id of
     * the owner of the document being rendered, test_id is the id of the test being rendered, and answers is a boolean
     * ("true" or "false") of whether the test should be rendered with or without answers.
     * <br>
     * Since Jetty runs request handlers concurrently, a client requesting every page of a 50-page PDF to be
     * rendered would cause the server to create 50 of the same PDFRenderer object at the same time (since the PDFRenderer
     * object only get placed into the cache after a request completes). To avoid this, a global Set of all PDFRenderer
     * objects in the process of being created is maintained as renderersBeingCreated. While one request handler is creating a PDFRenderer,
     * it puts that PDFRenderer's key into renderersBeingCreated. This causes any other request handlers looking for the same
     * PDFRenderer to wait until it appears in the cache, thus reducing the creation of superfluous PDFRenderers.
     */
    private static Map<String, PDFRenderer> pdfRendererCache;

    /**
     * renderersBeingCreated is used to indicate which PDFRenderers are in the process of being created. While a PDFRenderer is being created,
     * other request handlers that need the same PDFRenderer will busy-wait for the PDFRenderer to appear in the cache
     * (so as to avoid creating duplicate PDFRenderers, which are resource-intensive).
     * <br>
     * Since Jetty runs request handlers concurrently, a client requesting every page of a 50-page PDF to be
     * rendered would cause the server to create 50 of the same PDFRenderer object at the same time (since the PDFRenderer
     * object only get placed into the cache after a request completes). To avoid this, a global Set of all PDFRenderer
     * objects in the process of being created is maintained as renderersBeingCreated. While one request handler is creating a PDFRenderer,
     * it puts that PDFRenderer's key into renderersBeingCreated. This causes any other request handlers looking for the same
     * PDFRenderer to wait until it appears in the cache, thus reducing the creation of superfluous PDFRenderers.
     */
    private static Set<String> renderersBeingCreated;

    /**
     * Create a new RenderQuestionController.
     * @param persistentStorage The PersistentStorage object to use to store permanent data. Shared with all other controllers.
     * @param pdfRendererCache A cache of PDFRenderer objects shared between all controllers that render PDFs. Each one is keyed by a unique key. See controllers.RenderQuestionController.pdfRendererCache for more information.
     * @param renderersBeingCreated A set of all PDFRenderer objects currently in the process of being created. See controllers.RenderQuestionController.renderersBeingCreated for more information.
     */
    public RenderQuestionController(PersistentStorage persistentStorage, Map<String, PDFRenderer> pdfRendererCache, Set<String> renderersBeingCreated) {
        super(persistentStorage);
        this.pdfRendererCache = pdfRendererCache;
        this.renderersBeingCreated = renderersBeingCreated;
    }

    /**
     * Serve GET requests to /render_question. This API endpoint renders the raw PNG data of a specified region of
     * a page of a test. The requests require the following parameters:
     * <ul>
     *     <li>page - The page number to render from (an integer).
     *     <li>test_id - The id number of the test to render from (a positive integer).
     *     <li>answers - A boolean value (true or false) of whether to render the test with or without answers.
     *     <li>x - The x-coordinate in INCHES (not pixels) of the top-left coordinate of the desired region (a floating-point value greater than or equal to 0.0).
     *     <li>y - The y-coordinate in INCHES (not pixels) of the top-left coordinate of the desired region (a floating-point value greater than or equal to 0.0).
     *     <li>width - The width in INCHES (not pixels) of the desired region (a floating-point value, can be negative or positive).
     *     <li>height - The height in INCHES (not pixels) of the desired region (a floating-point value, can be negative or positive).
     *     <li>dpi - The dots-per-inch that the desired region should be rendered at (a positive integer).
     * </ul>
     */
    public static Route serveRenderQuestionPageGet = (Request request, Response response) -> {
        // Check that the user is valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get the parameters of the request.
        int page = Integer.parseInt(request.queryParamOrDefault("page", "-1"));
        if (page < 0) {
            return "ERROR: This API endpoint requires the 'page' parameter.";
        }
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id < 0) {
            return "ERROR: This API endpoint requires the 'test_id' parameter.";
        }
        boolean answers = Boolean.parseBoolean(request.queryParamOrDefault("answers", "false"));
        double x = Double.parseDouble(request.queryParamOrDefault("x", "NaN"));
        if (Double.isNaN(x)) {
            return "ERROR: This API endpoint requires the 'x' parameter (a double greater than or equal to 0.0).";
        }
        double y = Double.parseDouble(request.queryParamOrDefault("y", "NaN"));
        if (Double.isNaN(y)) {
            return "ERROR: This API endpoint requires the 'y' parameter (a double greater than or equal to 0.0).";
        }
        double width = Double.parseDouble(request.queryParamOrDefault("width", "NaN"));
        if (Double.isNaN(width)) {
            return "ERROR: This API endpoint requires the 'width' parameter (a double).";
        }
        double height = Double.parseDouble(request.queryParamOrDefault("height", "NaN"));
        if (Double.isNaN(height)) {
            return "ERROR: This API endpoint requires the 'height' parameter (a double).";
        }
        float dpi = Float.parseFloat(request.queryParamOrDefault("dpi", "-1.0"));
        if (dpi < 0.0) {
            return "ERROR: This API endpoint requires the 'dpi' parameter.";
        }
        // Check if a PDFRenderer has already been created and cached for the requested document.
        String rendererCacheKey = user_id + ":" + test_id + ":" + answers;
        PDFRenderer renderer;
        // If another parallel request processor is creating a renderer, wait for it:
        while (renderersBeingCreated.contains(rendererCacheKey)) {
            TimeUnit.MILLISECONDS.sleep(150);
        }
        // If a PDFRenderer has already been created, retrieve it.
        if (pdfRendererCache.containsKey(rendererCacheKey)) {
            System.out.println("Loading PDF renderer from cache with key: " + rendererCacheKey);
            renderer = pdfRendererCache.get(rendererCacheKey);
        }
        // If a PDFRenderer has not been created, retrieve the PDF from the database and create a renderer.
        else {
            renderersBeingCreated.add(rendererCacheKey);
            Test test = persistentStorage.getTestById(user_id, test_id);

            int test_file_id = -1;
            if (answers) {
                test_file_id = test.getAnswersTestFile();
            }
            else {
                test_file_id = test.getBlankTestFile();
            }

            TestFile test_file = persistentStorage.getTestFileById(user_id, test_file_id);

            byte[] test_file_data = test_file.getData();

            PDDocument document = PDDocument.load(test_file_data);

            renderer = new PDFRenderer(document);

            System.out.println("Creating PDF renderer with key: " + rendererCacheKey);
            pdfRendererCache.put(rendererCacheKey, renderer);
            renderersBeingCreated.remove(rendererCacheKey);
        }
        // Use the PDF renderer that was just created/retrieved from cache to render the question and send back the raw PNG data.
        BufferedImage image = renderer.renderImageWithDPI(page, dpi);
        BufferedImage question_image = image.getSubimage((int) (x * dpi), (int) (y * dpi), (int) (width * dpi), (int) (height * dpi));
        ByteArrayOutputStream png_data = new ByteArrayOutputStream();
        ImageIO.write(question_image, "PNG", png_data);
        byte[] png_data_raw = png_data.toByteArray();
        response.type("image/png");
        return png_data_raw;
    };
}
