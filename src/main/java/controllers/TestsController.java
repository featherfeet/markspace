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

/** @file TestsController.java
 * Controller for the /tests page that lists a user's tests (not to be confused with the /test page).
 * @see controllers.TestsController
 */

import spark.*;
import java.util.*;
import spark.template.velocity.*;
import storage.PersistentStorage;
import storage.Test;

/**
 * Controller for the /tests page. Used to view all tests created by a user. Serves as the "landing page" when a user is logged in.
 * Has links to create tests, delete tests, view test, upload student answers, and view student answers.
 */
public class TestsController extends Controller {
    /**
     * Create a new TestsController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public TestsController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to /tests. No parameters required. Renders a table of all tests created by the current user.
     * Has buttons to create/delete/view tests and student answers.
     */
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
        Map<Integer, Map<Integer, String>> student_answer_files_by_test_id = new HashMap<>(); // A map that associates test ID numbers with maps of student answer files. Each sub-map associates student answer files' IDs with their names.
        for (int test_id : test_ids) {
            Map<Integer, String> student_answer_files = persistentStorage.getStudentAnswerFilesByTestId(user_id, test_id);
            student_answer_files_by_test_id.put(test_id, student_answer_files);
        }
        model.put("student_answer_files_by_test_id", student_answer_files_by_test_id);
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/tests.vm"));
    };
}
