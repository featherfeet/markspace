package controllers;

import org.apache.pdfbox.pdmodel.PDDocument;
import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.PersistentStorage;
import storage.Test;

import javax.servlet.MultipartConfigElement;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UploadStudentAnswersController extends Controller {
    /**
     * Create a new generic Controller object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public UploadStudentAnswersController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    public Route serveUploadStudentAnswersPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This API endpoint requires the test_id parameter.";
        }
        Test test = persistentStorage.getTestById(user_id, test_id);
        if (test == null) {
            return "ERROR: The test you requested either does not exist or is owned by another user.";
        }

        Map<String, Object> model = new HashMap<>();
        model.put("test_name", test.getName());
        model.put("test_pages", persistentStorage.getNumberOfPagesInTestFileById(user_id, test.getAnswersTestFile()));
        model.put("test_id", test_id);

        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/upload_student_answers.vm"));
    };

    public Route serveUploadStudentAnswersPagePost = (Request request, Response response) -> {
        // Set up the jetty web server to accept file uploads. Max file size of 1 GB. Write all files larger than 1 MB to disk.
        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp", 1000000000, 1000000000, 1000000));
        // Validate the user.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get the test id of the test that these answers correspond to.
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This API endpoint requires the test_id parameter.";
        }
        Test test = persistentStorage.getTestById(user_id, test_id);
        if (test == null) {
            return "ERROR: The test you requested either does not exist or is owned by another user.";
        }
        // Get raw binary file data.
        byte[] student_answers_file_upload_data = null;
        String student_answers_file_upload_name = null;
        try (InputStream inputStream = request.raw().getPart("student_answers_file_upload").getInputStream()) {
            student_answers_file_upload_name = request.raw().getPart("student_answers_file_upload").getSubmittedFileName();
            student_answers_file_upload_data = new byte[inputStream.available()];
            inputStream.read(student_answers_file_upload_data);
        }
        if (student_answers_file_upload_data == null || student_answers_file_upload_name == null) {
            return "ERROR: You must provide the student_answers_file_upload and student_answers_file_name parameters to this API endpoint.";
        }
        // Save the form data as a new student answer file in the database.
        PDDocument student_answers_file_document = PDDocument.load(student_answers_file_upload_data);
        persistentStorage.createStudentAnswerFile(user_id, test_id, student_answers_file_upload_data, student_answers_file_upload_name, "pdf", student_answers_file_document.getNumberOfPages());
        // Send the user back to the tests page with a success message.
        request.session().attribute("message_color", "green");
        request.session().attribute("message", "Student answers <b>" + student_answers_file_upload_name + "</b> uploaded successfully to test <b>" + test.getName() + "</b>.");
        response.redirect("/tests");
        return "";
    };
}
