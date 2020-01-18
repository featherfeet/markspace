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

/** @file SignoutController.java
 * Controller for /signout.
 * @see controllers.SignoutController
 */

import spark.*;
import storage.PersistentStorage;

/**
 * Controller for /signout, used to sign users out of their accounts.
 */
public class SignoutController extends Controller {
    /**
     * Create a new SignoutController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public SignoutController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Handle GET requests to /signout. Signs the user out by clearing their session, then redirects back to /login.
     * @see controllers.LoginController
     */
    public static Route serveSignoutPageGet = (Request request, Response response) -> {
        String username = request.session().attribute("username");
        request.session().invalidate();
        response.redirect("/login");
        System.out.println("User " + username + " signed out successfully.");
        return "";
    };
}
