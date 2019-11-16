package controllers;

import spark.*;

import java.io.InputStream;
import java.util.*;
import spark.template.velocity.*;
import storage.PersistentStorage;

import javax.servlet.MultipartConfigElement;

public class CreateNewTest1Controller {
    private static PersistentStorage persistentStorage;

    public CreateNewTest1Controller(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveCreateNewTest1PageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        Map<String, Object> model = new HashMap<>();
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/create_new_test_1.vm"));
    };

    public static Route serveCreateNewTest1PagePost = (Request request, Response response) -> {
        // Set up the jetty web server to accept file uploads. Max file size of 1 GB. Write all files larger than 1 MB to disk.
        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp", 1000000000, 1000000000, 1000000));
        // Check for a valid user.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Get the data the user filled in the form with.
        byte[] answers_test_file_upload_data = null;
        byte[] blank_test_file_upload_data = null;
        String answers_test_file_upload_name = null;
        String blank_test_file_upload_name = null;
        try (InputStream inputStream = request.raw().getPart("answers_test_file_upload").getInputStream()) {
            answers_test_file_upload_name = request.raw().getPart("answers_test_file_upload").getSubmittedFileName();
            answers_test_file_upload_data = new byte[inputStream.available()];
            inputStream.read(answers_test_file_upload_data);
        }
        try (InputStream inputStream = request.raw().getPart("blank_test_file_upload").getInputStream()) {
            blank_test_file_upload_name = request.raw().getPart("blank_test_file_upload").getSubmittedFileName();
            blank_test_file_upload_data = new byte[inputStream.available()];
            inputStream.read(blank_test_file_upload_data);
        }
        String test_name = request.queryParams("test_name");
        String test_description = request.queryParams("test_description");
        // Save the form data as a new test in the database.
        int user_id = request.session().attribute("user_id");
        if (test_name != null && test_description != null && blank_test_file_upload_data != null && blank_test_file_upload_name != null && answers_test_file_upload_data != null && answers_test_file_upload_name != null) {
            int test_id = persistentStorage.createTest(user_id, test_name, test_description, blank_test_file_upload_data, blank_test_file_upload_name, "pdf", answers_test_file_upload_data, answers_test_file_upload_name, "pdf");
            response.redirect("/create_new_test_2?test_id=" + test_id);
        }
        return "";
    };
}
