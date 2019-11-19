package controllers;

/** @file IndexController.java
 * Controller for the / and /index pages.
 * @see controllers.IndexController
 */

import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.PersistentStorage;
import java.util.*;

/**
 * Controller for the / and /index pages.
 */
public class IndexController extends Controller {
    /**
     * Create a new IndexController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public IndexController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to / and /index. No parameters required. If the user is logged in, this redirects them to the /tests page. If not, it shows the homepage.
     */
    public static Route serveIndexPageGet = (Request request, Response response) -> {
        // If the user is valid, redirect them to the /tests page.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user != null && valid_user) {
            response.redirect("/tests");
        }
        // Otherwise, render the homepage.
        Map<String, Object> model = new HashMap<>();
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/index.vm"));
    };
}
