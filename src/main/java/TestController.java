import spark.*;
import spark.template.velocity.*;
import storage.PersistentStorage;
import storage.Test;

import java.util.*;

public class TestController {
    private static PersistentStorage persistentStorage;

    public TestController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveTestPageGet = (Request request, Response response) -> {
        // Check for valid user and get their user id.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get the id of the test being requested.
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: Need to provide the test_id parameter to specify which test you want to view.";
        }
        // Retrive the requested test from the database.
        Test test = persistentStorage.getTestById(user_id, test_id);
        // Render the template.
        Map<String, Object> model = new HashMap<>();
        model.put("test_name", test.getName());
        model.put("test_description", test.getDescription());
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/test.vm"));
    };
}
