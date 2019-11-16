package controllers;

import spark.*;
import storage.PersistentStorage;

public class SignoutController extends Controller {
    /**
     * Create a new generic Controller object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public SignoutController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    public static Route serveSignoutPageGet = (Request request, Response response) -> {
        String username = request.session().attribute("username");
        request.session().invalidate();
        response.redirect("/login");
        System.out.println("User " + username + " signed out successfully.");
        return "";
    };
}
