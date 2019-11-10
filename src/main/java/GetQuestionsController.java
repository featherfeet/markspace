import com.google.gson.Gson;
import spark.*;
import storage.PersistentStorage;
import storage.TestQuestion;

public class GetQuestionsController {
    private static Gson gson;
    private static PersistentStorage persistentStorage;

    public GetQuestionsController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
        this.gson = new Gson();
    }

    public static Route serveGetQuestionsPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        int test_file_id = Integer.parseInt(request.queryParamOrDefault("test_file_id", "-1"));
        if (test_id == -1 && test_file_id == -1) {
            return "ERROR: Must provide either test_id or test_file_id in the GET request to this API endpoint.";
        }
        response.type("application/json");
        if (test_id == -1) {
            TestQuestion[] testQuestions = persistentStorage.getQuestionsByTestFileId(user_id, test_file_id);
            return gson.toJson(testQuestions);
        }
        else if (test_file_id == -1) {
            TestQuestion[] testQuestions = persistentStorage.getQuestionsByTestId(user_id, test_id);
            return gson.toJson(testQuestions);
        }
        return gson.toJson(new TestQuestion[0]);
    };
}
