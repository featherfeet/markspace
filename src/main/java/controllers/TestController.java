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

/** @file TestController.java
 * Controller for the /test page (not to be confused with the /tests page). This page is used to view a specific test.
 * @see controllers.TestController
 */

import spark.*;
import spark.template.velocity.*;
import storage.PersistentStorage;
import storage.Test;

import java.util.*;

/**
 * Controller for the /test page (not to be confused with the /tests page). This page lets the user view a specific test by id.
 */
public class TestController extends Controller {
    /**
     * Create a new TestController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public TestController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to /test. Responds with a page that contains an HTML5 Canvas-based PDF viewer that renders using /render_test.
     * The GET requests require the following parameters:
     * <ul>
     *     <li>test_id - The id number (a positive integer) of the test to be viewed.</li>
     * </ul>
     * @see controllers.RenderTestController
     */
    public static Route serveTestPageGet = (Request request, Response response) -> {
        // Check for valid user and get their user id.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get the id of the test being requested.
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: Need to provide the test_id parameter to specify which test you want to view.";
        }
        // Retrieve the requested test from the database.
        Test test = persistentStorage.getTestById(user_id, test_id);
        if (test == null) {
            return "ERROR: No such test found. Does the test you are requesting belong to another user? If so, you must be signed in as that user to access it.";
        }
        // Render the template.
        Map<String, Object> model = new HashMap<>();
        model.put("test_name", test.getName());
        model.put("test_description", test.getDescription());
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/test.vm"));
    };
}
