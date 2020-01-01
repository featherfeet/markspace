package controllers;

import spark.*;
import storage.PersistentStorage;
import storage.StudentAnswer;
import storage.StudentAnswerSet;

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
        /* If this student answer was for an "identification" question
           (one that asks for the student's name/ID number), then label
           all other answers from this student answer set with the student's identity. */
        StudentAnswer studentAnswer = persistentStorage.getStudentAnswerById(user_id, student_answer_id);
        if (studentAnswer.getTestQuestion().isIdentificationQuestion()) {
            StudentAnswerSet studentAnswerSet = persistentStorage.findStudentAnswerSetWithStudentAnswer(user_id, student_answer_id);
            for (int other_student_answer_id : studentAnswerSet.getStudentAnswerIds()) {
                persistentStorage.identifyStudentAnswer(user_id, other_student_answer_id, score);
            }
        }
        return "";
    };
}
