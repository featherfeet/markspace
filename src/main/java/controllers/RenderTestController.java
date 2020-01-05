package controllers;

/** @file RenderTestController.java
 *  Controller for GET requests to the /render_test page.
 *  @see controllers.RenderTestController
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import spark.*;
import storage.PersistentStorage;
import storage.Test;
import storage.TestFile;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.TimeUnit;

/**
 * Controller to serve GET requests to the /render_test page.
 */
public class RenderTestController extends Controller {
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
     * Create a new RenderTestController object.
     * @param persistentStorage The PersistentStorage object to use to store permanent data. Shared with all other controllers.
     * @param pdfRendererCache A cache of PDFRenderer objects shared between all controllers that render PDFs. Each one is keyed by a unique key. See controllers.RenderTestController.pdfRendererCache for more information.
     * @param renderersBeingCreated A set of all PDFRenderer objects currently in the process of being created. See controllers.RenderTestController.renderersBeingCreated for more information.
     */
    public RenderTestController(PersistentStorage persistentStorage, Map<String, PDFRenderer> pdfRendererCache, Set<String> renderersBeingCreated) {
        super(persistentStorage);
        this.pdfRendererCache = pdfRendererCache;
        this.renderersBeingCreated = renderersBeingCreated;
    }

    /**
     * Serve GET requests to /render_test. This page either returns the number of pages in a test OR the raw, rendered PNG image data of a page of the test.
     * The GET requests must have the following parameters:
     * <ul>
     *     <li>test_id: The id number of the test to render from (a positive integer).</li>
     *     <li>answers: A true or false value of whether to render the test with or without answers.</li>
     *     <li>page: The page number of the test to render (a positive integer).</li>
     *     <li>dpi: The dots-per-inch (resolution) to render at (a positive integer).</li>
     *     <li>get_number_of_pages: A true or false value. If true, the response sent back to the browser will just be the number of pages in the test requested. If false, the response send back will be the rendered PNG image data of the requested page of the test.</li>
     * </ul>
     */
    public static Route serveRenderTestPageGet = (Request request, Response response) -> {
        // Check if the user is valid. If not, send them back to the login page.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }

        // Gather information about what page of what document the client wants rendered.
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        boolean answers = Boolean.parseBoolean(request.queryParamOrDefault("answers", "false"));
        int page = Integer.parseInt(request.queryParamOrDefault("page", "-1"));
        float dpi = Float.parseFloat(request.queryParamOrDefault("dpi", "-1"));
        boolean get_number_of_pages = Boolean.parseBoolean(request.queryParamOrDefault("get_number_of_pages", "false"));

        // If the client is requesting the number of pages in the document, send it back as a plain-text number and DON'T render anything.
        if (get_number_of_pages) {
            Test test = persistentStorage.getTestById(user_id, test_id);
            int test_file_id;
            if (answers) {
                test_file_id = test.getAnswersTestFile();
            }
            else {
                test_file_id = test.getBlankTestFile();
            }
            int number_of_pages = persistentStorage.getNumberOfPagesInTestFileById(user_id, test_file_id);
            response.type("text/plain");
            return String.valueOf(number_of_pages);
        }

        // If any of the required parameters were not supplied, send an error and stop.
        if (test_id == -1 || page == -1 || dpi == -1) {
            response.type("text/html");
            return "This API endpoint requires more parameters than were given.";
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

            pdfRendererCache.put(rendererCacheKey, renderer);
            renderersBeingCreated.remove(rendererCacheKey);
        }

        // Render the requested page of the PDF into PNG image data and send it back to the client for display.
        BufferedImage image = renderer.renderImageWithDPI(page, dpi);
        ByteArrayOutputStream png_data = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", png_data);
        byte[] png_data_raw = png_data.toByteArray();
        response.type("image/png");
        return png_data_raw;
    };
}
