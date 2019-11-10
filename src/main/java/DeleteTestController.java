import spark.*;
import storage.PersistentStorage;

public class DeleteTestController {
    private static PersistentStorage persistentStorage;

    public DeleteTestController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveDeleteTestPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }

        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This API endpoint requires the test_id parameter in the GET request.";
        }

        persistentStorage.deleteTestById(user_id, test_id);

        return "SUCCESS";
    };
}
