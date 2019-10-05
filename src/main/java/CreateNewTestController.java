import spark.*;
import java.util.*;
import spark.template.velocity.*;
import storage.PersistentStorage;

public class CreateNewTestController {
    private PersistentStorage persistentStorage;

    public CreateNewTestController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveCreateNewTestPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        Map<String, Object> model = new HashMap<>();
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/create_new_test.vm"));
    };
}
