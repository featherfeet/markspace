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

import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.PersistentStorage;
import storage.Test;

import java.util.HashMap;
import java.util.Map;

public class GradeTestController {
    private static PersistentStorage persistentStorage;

    public GradeTestController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public static Route serveGradeTestPageGet = (Request request, Response response) -> {
        // Validate the user and redirect them to /login if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Get the user id and test id of the test being requested. Then, use them to retrieve the requested test from storage.
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This page requires the test_id parameter.";
        }
        Test test = persistentStorage.getTestById(user_id, test_id);
        if (test == null) {
            return "ERROR: The test requested either does not exist or belongs to another user.";
        }
        // Render the test-grading page.
        Map<String, Object> model = new HashMap<>();
        model.put("test_name", test.getName());
        model.put("test_description", test.getDescription());
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/grade_test.vm"));
    };
}
