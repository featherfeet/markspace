package controllers;

/** @file LoginController.java
 * Controller for the /login page for logging users in.
 * @see controllers.LoginController
 */

import spark.*;
import java.util.*;
import spark.template.velocity.*;
import storage.PersistentStorage;

/**
 * Controller for the /login page for logging users in. GET requests show a form for logging in. POST requests submit that form and set up the session if the user is valid.
 */
public class LoginController extends Controller {
    /**
     * Create a new LoginController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public LoginController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to /login. No parameters required. Renders the login form.
     */
    public static Route serveLoginPageGet = (Request request, Response response) -> {
        // If the user is already logged in and valid, redirect them to the /tests page.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user != null && valid_user) {
            response.redirect("/tests");
        }
        // If the user is not already logged in, render the login form.
        Map<String, Object> model = new HashMap<>();
        /* If no session message has been set, set a blank message. The message and message_color session attributes
        should always be set. They are used to show messages on the next page that user goes to. */
        if (request.session().attribute("message") == null) {
            request.session().attribute("message", "");
        }
        if (request.session().attribute("message_color") == null) {
            request.session().attribute("message_color", "");
        }
        /* Add the message (with color) to the template to be rendered for the user.
        The message is used to indicate things like "user creation successful" or "login failed". */
        model.put("message", request.session().attribute("message"));
        model.put("message_color", request.session().attribute("message_color"));
        // Clear the message after it has been shown to the user once.
        request.session().attribute("message", "");
        request.session().attribute("message_color", "");
        // Render the login form (with any message that needs to be shown).
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/login.vm"));
    };

    /**
     * Serve POST requests to the /login page that are used to submit the login form. Sets up the session for users that
     * log in successfully. The following parameters are required for the POST request:<br>
     * <ul>
     *     <li>username - The username of the user being logged in.</li>
     *     <li>password - The password of the user being logged in.</li>
     * </ul>
     */
    public static Route serveLoginPagePost = (Request request, Response response) -> {
        // Check that the database is available.
        if (persistentStorage == null) {
            System.out.println("You must instantiate a controllers.LoginController object to be able to use the database.");
            return "";
        }
        // Get the username and password that the user filled in.
        String username = request.queryParamOrDefault("username", "");
        String password = request.queryParamOrDefault("password", "");
        // Check the database for whether the user is valid or not.
        int validated_user_id = persistentStorage.validateUser(username, password);
        // If the user was valid, set up their session.
        if (validated_user_id != -1) {
            System.out.println("User " + username + " logged in successfully.");
            request.session(true);
            request.session().attribute("username", username);
            request.session().attribute("valid_user", true);
            request.session().attribute("user_id", validated_user_id);
            // These two session attributes are used to flash messages to the user. They persist across multiple pages, making them useful for "wrong username" or "form submit successful" type messages.
            request.session().attribute("message_color", "");
            request.session().attribute("message", "");
            // Redirect to the tests page if the user logged in successfully.
            response.redirect("/tests");
        }
        // If the user did not log in successfully, set a red "login failed" message and redirect to /login.
        else {
            System.out.println("User " + username + " failed to log in.");
            request.session(true);
            request.session().attribute("username", username);
            request.session().attribute("valid_user", false);
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "Wrong user credentials. Please try again.");
            response.redirect("/login");
        }
        return "";
    };
}
