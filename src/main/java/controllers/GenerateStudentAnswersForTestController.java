package controllers;

import spark.*;
import storage.PersistentStorage;

public class GenerateStudentAnswersForTestController {
    private static PersistentStorage persistentStorage;

    public GenerateStudentAnswersForTestController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public Route serveGenerateStudentAnswersForTestPageGet = (Request request, Response response) -> {
        // Validate the user and redirect them to /login if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get request parameters.
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This API endpoint requires the test_id parameter.";
        }

        return "";
    };
}
