import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.PersistentStorage;
import java.util.*;

public class IndexController {
    private static PersistentStorage persistentStorage;

    public IndexController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveIndexPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user != null && valid_user) {
            response.redirect("/tests");
        }
        Map<String, Object> model = new HashMap<>();
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/index.vm"));
    };
}
