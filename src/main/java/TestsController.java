import spark.*;
import java.util.*;
import spark.template.velocity.*;
import storage.PersistentStorage;
import storage.Test;

public class TestsController {
    private static PersistentStorage persistentStorage = null;

    public TestsController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveTestsPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        Map<String, Object> model = new HashMap<>();
        Test[] tests = persistentStorage.getTestsByUser(request.session().attribute("user_id"));
        String[] test_names = new String[tests.length];
        for (int i = 0; i < tests.length; i++) {
            test_names[i] = tests[i].getName();
        }
        model.put("test_names", test_names);
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/tests.vm"));
    };
}
