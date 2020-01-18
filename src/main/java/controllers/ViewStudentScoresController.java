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

import org.apache.velocity.tools.generic.MathTool;
import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.*;

import java.util.HashMap;
import java.util.Map;

public class ViewStudentScoresController {
    private static PersistentStorage persistentStorage;

    public ViewStudentScoresController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public Route serveViewStudentScoresPageGet = (Request request, Response response) -> {
        // Validate the user and redirect them if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Get request options.
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: The test_id parameter is required for this API endpoint.";
        }
        // Calculate students' scores.
        StudentScores studentScores = new StudentScores(persistentStorage, user_id, test_id);
        // Generate a page showing calculated student test scores.
        Map<String, Object> model = new HashMap<>();
        model.put("test_id", test_id);
        model.put("test_name", studentScores.getTest().getName());
        model.put("test_possible_points", studentScores.getTestPossiblePoints());
        model.put("student_identifications", studentScores.getStudentIdentifications());
        model.put("student_point_scores", studentScores.getStudentPointScores());
        model.put("student_percentage_scores", studentScores.getStudentPercentageScores());
        model.put("student_letter_grades", studentScores.getStudentLetterGrades());
        model.put("end_student_index", studentScores.getStudentIdentifications().size() - 1);
        model.put("math", new MathTool());
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/view_student_scores.vm"));
    };
}
