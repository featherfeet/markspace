package controllers;

/** @file RenderStudentAnswerController.java
 * Controller for /render_student_answer.
 * @see controllers.RenderStudentAnswerController
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import spark.Request;
import spark.Response;
import spark.Route;
import storage.PersistentStorage;
import util.Rectangle;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Controller to handle GET requests to /render_student_answer.
 */
public class RenderStudentAnswerController extends Controller {
    /**
     * pdfRendererCache is used to cache PDFRenderer objects used to render student answers.
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
     */
    public RenderStudentAnswerController(PersistentStorage persistentStorage) {
        super(persistentStorage);
        this.pdfRendererCache = new HashMap<>();
        this.renderersBeingCreated = new HashSet<>();
    }

    /**
     * Serve GET requests to /render_student_answer. This API endpoint renders the raw PNG data of a specified region of
     * a page of a student answer file. The requests require the following parameters:
     * <ul>
     *     <li>page - The page number to render from (an integer).
     *     <li>student_answer_file_id - The id number of the test to render from (a positive integer).
     *     <li>x - The x-coordinate in INCHES (not pixels) of the top-left coordinate of the desired region (a floating-point value greater than or equal to 0.0).
     *     <li>y - The y-coordinate in INCHES (not pixels) of the top-left coordinate of the desired region (a floating-point value greater than or equal to 0.0).
     *     <li>width - The width in INCHES (not pixels) of the desired region (a floating-point value, can be negative or positive).
     *     <li>height - The height in INCHES (not pixels) of the desired region (a floating-point value, can be negative or positive).
     *     <li>dpi - The dots-per-inch that the desired region should be rendered at (a positive integer).
     * </ul>
     */
    public static Route serveRenderStudentAnswerPageGet = (Request request, Response response) -> {
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
        int student_answer_file_id = Integer.parseInt(request.queryParamOrDefault("student_answer_file_id", "-1"));
        if (student_answer_file_id < 0) {
            return "ERROR: This API endpoint requires the 'student_answer_file_id' parameter.";
        }
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
        String rendererCacheKey = user_id + ":" + student_answer_file_id;
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

            byte[] student_answer_file_data = persistentStorage.getStudentAnswerFileById(user_id, student_answer_file_id);

            PDDocument document = PDDocument.load(student_answer_file_data);

            renderer = new PDFRenderer(document);

            pdfRendererCache.put(rendererCacheKey, renderer);
            renderersBeingCreated.remove(rendererCacheKey);
        }
        // Use the PDF renderer that was just created/retrieved from cache to render the question and send back the raw PNG data.
        // Render an image of the page.
        BufferedImage image = renderer.renderImageWithDPI(page, dpi);
        // Retrieve the requested region of the page.
        int x_pixels = (int) (x * dpi);
        int y_pixels = (int) (y * dpi);
        int width_pixels = (int) (width * dpi);
        int height_pixels = (int) (height * dpi);
        Rectangle image_region = new Rectangle(x_pixels, y_pixels, width_pixels, height_pixels);
        image_region.convertToTopLeftRectangle();
        image_region.cropToBounds(image.getWidth(), image.getHeight());
        BufferedImage student_answer_image = image.getSubimage(image_region.getX(), image_region.getY(), image_region.getWidth(), image_region.getHeight());
        // Convert the requested region into raw PNG data to send back to the client.
        ByteArrayOutputStream png_data = new ByteArrayOutputStream();
        ImageIO.write(student_answer_image, "PNG", png_data);
        byte[] png_data_raw = png_data.toByteArray();
        response.type("image/png");
        return png_data_raw;
    };
}
