package controllers;

import com.google.gson.Gson;
import spark.*;
import storage.PersistentStorage;
import storage.StudentAnswer;
import storage.TestQuestion;

import java.util.*;

public class GetStudentAnswersForTestController {
    private static Gson gson;
    private static PersistentStorage persistentStorage;

    public GetStudentAnswersForTestController(PersistentStorage persistentStorage) {
        this.gson = new Gson();
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
        int student_answer_file_id = Integer.parseInt(request.queryParamOrDefault("student_answer_file_id", "-1"));
        List<Integer> student_answer_file_ids = new ArrayList<>();
        if (student_answer_file_id == -1) {
            // If no student answer file ID was provided, fetch ALL student answer files for this test.
            student_answer_file_ids.addAll(persistentStorage.getStudentAnswerFilesNumberOfPages(user_id, test_id).keySet());
        }
        else {
            // If a student answer file ID was provided, only use that file.
            student_answer_file_ids.add(student_answer_file_id);
        }
        // Retrieve the empty answers that we just created from the database. They will now have ID's attached to them.
        List<StudentAnswer> student_answers_with_ids = new ArrayList<>();
        for (int current_student_answer_file_id : student_answer_file_ids) {
            student_answers_with_ids.addAll(persistentStorage.getStudentAnswersByStudentAnswerFileId(user_id, current_student_answer_file_id));
        }
        // Send back the student answer objects with IDs as JSON.
        return gson.toJson(student_answers_with_ids.toArray());
    };
}
