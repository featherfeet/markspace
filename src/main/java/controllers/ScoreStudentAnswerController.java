package controllers;

import spark.*;
import storage.PersistentStorage;

public class ScoreStudentAnswerController {
    private static PersistentStorage persistentStorage;

    public ScoreStudentAnswerController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public Route serveScoreStudentAnswerPagePost = (Request request, Response response) -> {
        // Validate the user and redirect them if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Get request parameters.
        int user_id = request.session().attribute("user_id");
        int student_answer_id = Integer.parseInt(request.queryParamOrDefault("student_answer_id", "-1"));
        if (student_answer_id == -1) {
            return "ERROR: The student_answer_id parameter is required for this API endpoint.";
        }
        if (!request.queryMap().hasKey("score")) {
            return "ERROR: THe score parameter is required for this API endpoint.";
        }
        String score = request.queryParams("score");
        // Save the score to the database.
        persistentStorage.scoreStudentAnswer(user_id, student_answer_id, score);
        return "";
    };
}
