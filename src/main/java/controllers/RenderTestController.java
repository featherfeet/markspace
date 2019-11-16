package controllers;

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

public class RenderTestController extends Controller {
    private static Set<String> renderersBeingCreated;
    private static Map<String, PDFRenderer> pdfRendererCache;

    public RenderTestController(PersistentStorage persistentStorage, Map<String, PDFRenderer> pdfRendererCache, Set<String> renderersBeingCreated) {
        super(persistentStorage);
        this.pdfRendererCache = pdfRendererCache;
        this.renderersBeingCreated = renderersBeingCreated;
    }

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

        // If the client is requesting the number of pages in the document, send it back and DON'T render anything.
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
            response.type("text/html");
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

        // Render the requested page of the PDF into PNG image data.
        BufferedImage image = renderer.renderImageWithDPI(page, dpi);
        ByteArrayOutputStream png_data = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", png_data);
        byte[] png_data_raw = png_data.toByteArray();
        response.type("image/png");
        return png_data_raw;
    };
}
