import spark.*;
import java.util.*;
import spark.template.velocity.*;
import storage.PersistentStorage;

public class LoginController {
    private static PersistentStorage persistentStorage = null;

    public LoginController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveLoginPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user != null && valid_user) {
            response.redirect("/tests");
        }
        Map<String, Object> model = new HashMap<>();
        if (request.session().attribute("message") == null) {
            request.session().attribute("message", "");
        }
        if (request.session().attribute("message_color") == null) {
            request.session().attribute("message_color", "");
        }
        model.put("message", request.session().attribute("message"));
        model.put("message_color", request.session().attribute("message_color"));
        request.session().attribute("message", "");
        request.session().attribute("message_color", "");
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/login.vm"));
    };

    public static Route serveLoginPagePost = (Request request, Response response) -> {
        if (persistentStorage == null) {
            System.out.println("You must instantiate a LoginController object to be able to use the database.");
            return "";
        }
        String username = request.queryParams("username");
        String password = request.queryParams("password");
        int validated_user_id = persistentStorage.validateUser(username, password);
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