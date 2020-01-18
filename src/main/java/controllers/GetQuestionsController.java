/*
 * Copyright 2020 Oliver Trevor and Suchin Ravi.
 *
 * This file is part of MarkSpace.
 *
 * MarkSpace is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MarkSpace is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MarkSpace.  If not, see <https://www.gnu.org/licenses/>.
 */

package controllers;

/** @file GetQuestionsController.java
 * Controller for the /get_questions page.
 * @see controllers.GetQuestionsController
 */

import com.google.gson.Gson;
import spark.*;
import storage.PersistentStorage;
import storage.TestQuestion;

/**
 * Controller for GET requests to the /get_questions page.
 */
public class GetQuestionsController extends Controller {
    /**
     * A reusable, shared Google Gson object for processing JSON.
     */
    private static Gson gson;

    /**
     * Create a new GetQuestionsController object with a shared PersistentStorage. MUST be called before using the controller.
     * @param persistentStorage The PersistentStorage object to use to store permanent data. Shared with all other controllers.
     */
    public GetQuestionsController(PersistentStorage persistentStorage) {
        super(persistentStorage);
        this.gson = new Gson();
    }

    /**
     * Serve GET requests to /get_questions. A GET request to this endpoint must have EITHER the test_id parameter to specify which test questions should be retrieved from or the
     * test_file_id parameter to specify which test file (a specific uploaded PDF) questions should be retrieved from. The response will be a JSON array of test questions.
     */
    public static Route serveGetQuestionsPageGet = (Request request, Response response) -> {
        // Validate the user and redirect them if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get the request parameters.
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        int test_file_id = Integer.parseInt(request.queryParamOrDefault("test_file_id", "-1"));
        // If neither test_id nor test_file_id was provided, error.
        if (test_id == -1 && test_file_id == -1) {
            return "ERROR: Must provide either test_id or test_file_id in the GET request to this API endpoint.";
        }
        // We're sending JSON data of the questions back.
        response.type("application/json");
        // If the request is by test_file_id and test_id was not provided, then get the questions and send them back as JSON.
        if (test_id == -1) {
            TestQuestion[] testQuestions = persistentStorage.getQuestionsByTestFileId(user_id, test_file_id);
            return gson.toJson(testQuestions);
        }
        // If the request is by test_id and test_file_id was not provided, then get the questions and send them back as JSON.
        else if (test_file_id == -1) {
            TestQuestion[] testQuestions = persistentStorage.getQuestionsByTestId(user_id, test_id);
            return gson.toJson(testQuestions);
        }
        // Not actually reachable. Returns an empty JSON.
        return gson.toJson(new TestQuestion[0]);
    };
}
