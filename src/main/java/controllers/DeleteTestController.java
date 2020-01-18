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

/**
 * @file DeleteTestController.java
 * Controller for the /delete_test page.
 * @see controllers.DeleteTestController
 */

import spark.*;
import storage.PersistentStorage;

/**
 * Controller for GET requests to the /delete_test page, used to delete tests that have already been created.
 */
public class DeleteTestController extends Controller {
    /**
     * Create a new DeleteTestController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public DeleteTestController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Handle GET requests to /delete_test. The GET requests must have the following parameters:
     * <ul>
     *     <li>test_id - The ID number (from the database) of the test to be deleted.</li>
     * </ul>
     */
    public static Route serveDeleteTestPageGet = (Request request, Response response) -> {
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }

        int user_id = request.session().attribute("user_id");
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This API endpoint requires the test_id parameter in the GET request.";
        }

        persistentStorage.deleteTestById(user_id, test_id);

        return "SUCCESS";
    };
}
