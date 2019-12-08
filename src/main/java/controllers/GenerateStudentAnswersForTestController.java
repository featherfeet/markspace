package controllers;

import com.google.gson.Gson;
import spark.*;
import storage.PersistentStorage;
import storage.StudentAnswer;
import storage.TestQuestion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateStudentAnswersForTestController {
    private static Gson gson;
    private static PersistentStorage persistentStorage;

    public GenerateStudentAnswersForTestController(PersistentStorage persistentStorage) {
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
        // Fetch test questions from database.
        TestQuestion[] testQuestions = persistentStorage.getQuestionsByTestId(user_id, test_id);
        // Fetch student answer files.
        Map<Integer, Integer> studentAnswerFilesNumberOfPages = persistentStorage.getStudentAnswerFilesNumberOfPages(user_id, test_id);
        // If a specific student answer file was requested, remove the other ones.
        if (student_answer_file_id != -1 && studentAnswerFilesNumberOfPages.containsKey(student_answer_file_id)) {
            int student_answer_file_number_of_pages = studentAnswerFilesNumberOfPages.get(student_answer_file_id);
            studentAnswerFilesNumberOfPages = new HashMap<>();
            studentAnswerFilesNumberOfPages.put(student_answer_file_id, student_answer_file_number_of_pages);
        }
        // For each student answer file, generate student answer objects.
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        for (int current_student_answer_file_id : studentAnswerFilesNumberOfPages.keySet()) {
            // Find out how many pages are in the current student answer file.
            int current_student_answer_file_number_of_pages = studentAnswerFilesNumberOfPages.get(current_student_answer_file_id);
            // For each page of the current student answer file, find all test questions and create student answer objects from them.
            for (int page = 0; page < current_student_answer_file_number_of_pages; page++) {
                for (TestQuestion testQuestion : testQuestions) {
                    if (testQuestion.getPage() == page) {
                        StudentAnswer studentAnswer = new StudentAnswer(current_student_answer_file_id, testQuestion, "0.0", testQuestion.getPoints(), page);
                        studentAnswers.add(studentAnswer);
                    }
                }
            }
        }
        // Send back the array of empty (not yet graded) student answers as JSON.
        return gson.toJson(studentAnswers.toArray());
    };
}
