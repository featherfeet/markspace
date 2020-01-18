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

/** @file IndexController.java
 * Controller for the / and /index pages.
 * @see controllers.IndexController
 */

import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.PersistentStorage;
import java.util.*;

/**
 * Controller for the / and /index pages.
 */
public class IndexController extends Controller {
    /**
     * Create a new IndexController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public IndexController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to / and /index. No parameters required. If the user is logged in, this redirects them to the /tests page. If not, it shows the homepage.
     */
    public static Route serveIndexPageGet = (Request request, Response response) -> {
        // If the user is valid, redirect them to the /tests page.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user != null && valid_user) {
            response.redirect("/tests");
        }
        // Otherwise, render the homepage.
        Map<String, Object> model = new HashMap<>();
        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/index.vm"));
    };
}
