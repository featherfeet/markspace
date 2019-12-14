package controllers;

/** @file CreateNewTest2Controller.java
 * Controller for the /create_new_test_2 page.
 * @see controllers.CreateNewTest2Controller
 */

import spark.*;
import spark.template.velocity.*;
import storage.PersistentStorage;
import storage.Test;
import java.util.*;
import com.google.json.JsonSanitizer;
import com.google.gson.Gson;
import storage.TestQuestion;

/**
 * Controller for the /create_new_test_2 page. This page shows the test (with answers) and allows the teacher to select
 * regions of the pages to be test questions. It also allows the teacher to assign point values to each question.
 */
public class CreateNewTest2Controller extends Controller {
    /**
     * This uses the Google Gson library to decode JSON. To reduce overhead, we only create one Gson object per controller.
     */
    private static Gson gson;

    /**
     * Construct a new controller with the given PersistentStorage.
     * @param persistentStorage A PersistentStorage object shared between all controllers and used to store permanent data.
     */
    public CreateNewTest2Controller(PersistentStorage persistentStorage) {
        super(persistentStorage);
        this.gson = new Gson();
    }

    /**
     * Handle GET requests to /create_new_test_2. This handler shows the user interface that allows the teacher to create
     * test questions. The GET request must have the following parameters:
     * <ul>
     *     <li>test_id: The ID number (from the database) of the test that questions are being created for.</li>
     * </ul>
     */
    public static Route serveCreateNewTest2PageGet = (Request request, Response response) -> {
        // Validate the user and redirect them to /login if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Retrieve the ID of the current user and the ID of the test for which questions are being created.
        Map<String, Object> model = new HashMap<>();
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: test_id not provided in query.";
        }
        // Retrieve the requested test from the database.
        Test currentTest = persistentStorage.getTestById(user_id, test_id);
        if (currentTest == null) {
            return "ERROR: The test you are trying to access either does not exist or does not belong to your user.";
        }
        model.put("test_name", currentTest.getName());
        // Render the page. The PDF of the test itself will be rendered by /render_test (RenderTestController) via AJAX requests from the user's browser.
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/create_new_test_2.vm"));
    };

    /**
     * Handle POST requests submitting the form to /create_new_test_2. This saves all test questions that the teacher
     * created to the database. The POST request must have the following parameters:
     * <ul>
     *     <li>test_id - The ID number (from the database) of the test for which questions are being created.</li>
     *     <li>test_questions_json - A JSON string of all of the questions that the user created. This will be a JSON array of TestQuestion objects (defined in src/main/resources/static/scripts/typescript/testquestion.ts and in src/main/java/storage/TestQuestion.java).</li>
     * </ul>
     */
    public static Route serveCreateNewTest2PagePost = (Request request, Response response) -> {
        // Validate the user and redirect them if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Get the current user and the test for which questions are being created.
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: test_id not provided in query.";
        }
        // Retrieve the test from the database.
        Test test = persistentStorage.getTestById(user_id, test_id);
        // Get the ID of the previously-uploaded PDF of the test with answers.
        int test_file_id = test.getAnswersTestFile();
        // Get the JSON describing the test questions.
        String test_questions_json_raw = request.queryParamOrDefault("test_questions_json", "not_provided");
        if (test_questions_json_raw.equals("not_provided")) {
            return "ERROR: test_questions_json not provided in query.";
        }
        // Use JsonSanitizer to remove malicious/dangerous/malformed JSON.
        String test_questions_json = JsonSanitizer.sanitize(test_questions_json_raw);
        // Use Google Gson to convert the JSON into an array of Java objects.
        TestQuestion[] test_questions = gson.fromJson(test_questions_json, TestQuestion[].class);
        // Save the TestQuestion objects to the database.
        persistentStorage.createQuestions(test_file_id, user_id, test_questions);
        // Redirect the user back to the /tests page with a green message notifying them that the test was created successfully.
        request.session().attribute("message_color", "green");
        request.session().attribute("message", "Test &lsquo;" + test.getName() + "&rsquo; has been created successfully.");
        response.redirect("/tests");
        return "";
    };
}
