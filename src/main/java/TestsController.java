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
        // Check that the user is valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Retrieve tests from the database and put them into the template.
        Map<String, Object> model = new HashMap<>();
        Test[] tests = persistentStorage.getTestsByUser(request.session().attribute("user_id"));
        String[] test_names = new String[tests.length];
        String[] test_descriptions = new String[tests.length];
        int[] test_ids = new int[tests.length];
        for (int i = 0; i < tests.length; i++) {
            test_names[i] = tests[i].getName();
            test_descriptions[i] = tests[i].getDescription();
            test_ids[i] = tests[i].getId();
        }
        model.put("message_color", request.session().attribute("message_color"));
        model.put("message", request.session().attribute("message"));
        request.session().attribute("message_color", "");
        request.session().attribute("message", "");
        model.put("test_names", test_names);
        model.put("test_descriptions", test_descriptions);
        model.put("test_ids", test_ids);
        // Retrieve student answer files from the database and put them into the template.
        Map<Integer, Map<Integer, String>> student_answer_files_by_test_id = new HashMap<>();
        for (int test_id : test_ids) {
            Map<Integer, String> student_answer_files = persistentStorage.getStudentAnswerFilesByTestId(user_id, test_id);
            student_answer_files_by_test_id.put(test_id, student_answer_files);
        }
        model.put("student_answer_files_by_test_id", student_answer_files_by_test_id);
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/tests.vm"));
    };
}
