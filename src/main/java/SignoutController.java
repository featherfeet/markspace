import spark.*;
import storage.PersistentStorage;

public class SignoutController {
    private static PersistentStorage persistentStorage;

    public SignoutController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveSignoutPageGet = (Request request, Response response) -> {
        String username = request.session().attribute("username");
        request.session().invalidate();
        response.redirect("/login");
        System.out.println("User " + username + "logged out successfully.");
        return "";
    };
}
