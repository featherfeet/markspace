import spark.*;
import spark.template.velocity.*;
import storage.PersistentStorage;
import storage.Test;
import java.util.*;
import com.google.json.JsonSanitizer;
import com.google.gson.Gson;
import storage.TestQuestion;

public class CreateNewTest2Controller {
    private static Gson gson;
    private static PersistentStorage persistentStorage;

    public CreateNewTest2Controller(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
        this.gson = new Gson();
    }

    public static Route serveCreateNewTest2PageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        Map<String, Object> model = new HashMap<>();
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParams("test_id"));
        System.out.println("Starting query for test with id " + test_id);
        Test currentTest = persistentStorage.getTestById(user_id, test_id);
        System.out.println("Finished query. currentTest = " + currentTest);
        if (currentTest == null) {
            return "ERROR: The test you are trying to access either does not exist or does not belong to your user.";
        }
        model.put("test_name", currentTest.getName());
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/create_new_test_2.vm"));
    };

    public static Route serveCreateNewTest2PagePost = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: test_id not provided in query.";
        }
        String test_questions_json_raw = request.queryParamOrDefault("test_questions_json", "not_provided");
        if (test_questions_json_raw.equals("not_provided")) {
            return "ERROR: test_questions_json not provided in query.";
        }
        String test_questions_json = JsonSanitizer.sanitize(test_questions_json_raw);
        TestQuestion[] test_questions = gson.fromJson(test_questions_json, TestQuestion[].class);
        return "";
    };
}
