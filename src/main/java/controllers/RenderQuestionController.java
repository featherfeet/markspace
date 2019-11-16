package controllers;

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

public class RenderQuestionController {
    private static PersistentStorage persistentStorage;
    private static Map<String, PDFRenderer> pdfRendererCache;
    private static Set<String> renderersBeingCreated;

    public RenderQuestionController(PersistentStorage persistentStorage, Map<String, PDFRenderer> pdfRendererCache, Set<String> renderersBeingCreated) {
        this.persistentStorage = persistentStorage;
        this.pdfRendererCache = pdfRendererCache;
        this.renderersBeingCreated = renderersBeingCreated;
    }

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
        double x = Double.parseDouble(request.queryParamOrDefault("x", "-1.0"));
        if (x < 0.0) {
            return "ERROR: This API endpoint requires the 'x' parameter (a double greater than or equal to 0.0).";
        }
        double y = Double.parseDouble(request.queryParamOrDefault("y", "-1.0"));
        if (y < 0.0) {
            return "ERROR: This API endpoint requires the 'y' parameter (a double greater than or equal to 0.0).";
        }
        double width = Double.parseDouble(request.queryParamOrDefault("width", "-1.0"));
        if (width < 0.0) {
            return "ERROR: This API endpoint requires the 'width' parameter (a double greater than or equal to 0.0).";
        }
        double height = Double.parseDouble(request.queryParamOrDefault("height", "-1.0"));
        if (height < 0.0) {
            return "ERROR: This API endpoint requires the 'height' parameter (a double greater than or equal to 0.0).";
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
