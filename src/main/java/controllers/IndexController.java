package controllers;

import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.PersistentStorage;
import java.util.*;

public class IndexController extends Controller {
    /**
     * Create a new generic Controller object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public IndexController(PersistentStorage persistentStorage) {
        super(persistentStorage);
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
