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

import com.opencsv.CSVWriter;
import spark.*;
import storage.PersistentStorage;
import storage.StudentScores;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class DownloadStudentScoresController {
    private static PersistentStorage persistentStorage;

    public DownloadStudentScoresController(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public Route serveDownloadStudentScoresPageGet = (Request request, Response response) -> {
        // Validate the user and redirect them to /login if they are not valid.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        // Set the datatype of the response to CSV data.
        response.type("text/csv");
        // Get request parameters.
        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This API endpoint requires the test_id parameter.";
        }
        // Calculate student scores.
        StudentScores studentScores = new StudentScores(persistentStorage, user_id, test_id);
        // Suggest a filename for when the user's browser downloads the CSV.
        response.header("content-disposition", "attachment; filename=\"" + studentScores.getTest().getName() + " Scores.csv\"");
        // Prepare a CSV writer.
        StringWriter csvStringWriter = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(csvStringWriter);
        // Write the "title" row of the CSV.
        String[] title_row = {"Student Identification", "Point Score Out of " + studentScores.getTestPossiblePoints() + " Possible", "Percentage Score", "Letter Grade"};
        csvWriter.writeNext(title_row);
        // Generate data for the CSV writer.
        List<String[]> studentScoresData = new ArrayList<>();
        List<String> student_identifications = studentScores.getStudentIdentifications();
        List<Double> student_point_scores = studentScores.getStudentPointScores();
        List<Double> student_percentage_scores = studentScores.getStudentPercentageScores();
        List<String> student_letter_grades = studentScores.getStudentLetterGrades();
        for (int i = 0; i < studentScores.getStudentIdentifications().size(); i++) {
            List<String> studentScoreData = new ArrayList<>();
            studentScoreData.add(student_identifications.get(i));
            studentScoreData.add(String.valueOf(student_point_scores.get(i)));
            studentScoreData.add(String.valueOf(student_percentage_scores.get(i)));
            studentScoreData.add(student_letter_grades.get(i));
            String[] studentScoreDataTemp = new String[studentScoreData.size()];
            studentScoreData.toArray(studentScoreDataTemp);
            studentScoresData.add(studentScoreDataTemp);
        }
        // Add data to the CSV writer.
        csvWriter.writeAll(studentScoresData);
        // Return the raw CSV data.
        return csvStringWriter.toString();
    };
}
