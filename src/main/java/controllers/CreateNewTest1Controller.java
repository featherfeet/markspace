/*
 * Copyright 2020 Oliver Trevor and Suchin Ravi.
 *
 * This file is part of MarkSpace.
 *
 * MarkSpace is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MarkSpace is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MarkSpace.  If not, see <https://www.gnu.org/licenses/>.
 */

package controllers;

/**
 * @file CreateNewTest1Controller.java
 * A controller for the /create_new_test_1 page.
 * @see controllers.CreateNewTest1Controller
 */

import spark.*;

import java.io.InputStream;
import java.util.*;
import spark.template.velocity.*;
import storage.PersistentStorage;

import javax.servlet.MultipartConfigElement;

/**
 * This controller class handles GET and POST requests to the /create_new_test_1 page. GET requests show a form for
 * uploading the PDFs, name, and description of a test. POST requests are used to submit that form.
 */
public class CreateNewTest1Controller extends Controller {
    /**
     * Create a CreateNewTest1Controller object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public CreateNewTest1Controller(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to /create_new_test_1.
     * Redirect back to /login with a red message if the user is not logged in.
     * Otherwise, show the form.
     */
    public static Route serveCreateNewTest1PageGet = (Request request, Response response) -> {
        // Validate the user. Redirect them and prevent them seeing the form if they are not logged in.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Render the form and send it back.
        Map<String, Object> model = new HashMap<>();
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/create_new_test_1.vm"));
    };

    /**
     * Serve POST requests to /create_new_test_1 (these requests submit the form in templates/create_new_test_1.vm).
     * Redirect back to /login with a red message if the user is not logged in.
     * The following parameters are required for the POST request:
     * <ul>
     *     <li>answers_test_file_upload - The uploaded PDF file of the test, with correct answers written in.</li>
     *     <li>blank_test_file_upload - The uploaded PDF file of the test, without answers written in.</li>
     *     <li>test_name - The human-readable name/title of the test.</li>
     *     <li>test_description - A human-readable long description of the test.</li>
     * </ul>
     */
    public static Route serveCreateNewTest1PagePost = (Request request, Response response) -> {
        // Set up the jetty web server to accept file uploads. Max file size of 1 GB. Temporarily write all files larger than 1 MB to disk.
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
            // After saving the test to the database, redirect the user to the page for creating test questions (/create_new_test_2).
            response.redirect("/create_new_test_2?test_id=" + test_id);
        }
        return "";
    };
}
