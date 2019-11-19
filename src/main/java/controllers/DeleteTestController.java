package controllers;

/**
 * @file DeleteTestController.java
 * Controller for the /delete_test page.
 * @see controllers.DeleteTestController
 */

import spark.*;
import storage.PersistentStorage;

/**
 * Controller for GET requests to the /delete_test page, used to delete tests that have already been created.
 */
public class DeleteTestController extends Controller {
    /**
     * Create a new DeleteTestController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public DeleteTestController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Handle GET requests to /delete_test. The GET requests must have the following parameters:
     * <ul>
     *     <li>test_id - The ID number (from the database) of the test to be deleted.</li>
     * </ul>
     */
    public static Route serveDeleteTestPageGet = (Request request, Response response) -> {
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
            return "ERROR: This API endpoint requires the test_id parameter in the GET request.";
        }

        persistentStorage.deleteTestById(user_id, test_id);

        return "SUCCESS";
    };
}
