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
 * @file SignupController.java
 * Controller for the /signup page.
 * @see controllers.SignupController
 */

import spark.*;
import spark.template.velocity.*;
import java.util.*;
import storage.PersistentStorage;
import storage.UserPermission;

/**
 * Controller for the /signup page. Used to sign up (create accounts for) new users.
 */
public class SignupController extends Controller {
	/**
	 * Create a new SignupController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
	 *
	 * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
	 */
	public SignupController(PersistentStorage persistentStorage) {
		super(persistentStorage);
	}

	/**
	 * Serve GET requests to /signup. Renders the form for signing up for a new account.
	 */
	public static Route serveSignupPageGet = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		model.put("message", "");
		return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/signup.vm"));
	};

	/**
	 * Serve POST requests to /signup. Used to submit the form in templates/signup.vm.
	 * POST requests must have the following parameters:
	 * <ul>
	 *     <li>username - The username of the user being created.</li>
	 *     <li>password - The password of the user being created.</li>
	 *     <li>duplicate_password - Must match the password parameter. Used to prevent typos in entering a password.</li>
	 *     <li>email - The email of the user being created.</li>
	 * </ul>
	 */
	public static Route serveSignupPagePost = (Request request, Response response) -> {
		// Validate the form input.
		boolean validated_input = true;
		Map<String, Object> model = new HashMap<>();
		model.put("message", "");
		String username = request.queryParams("username");
		String password = request.queryParams("password");
		String duplicate_password = request.queryParams("duplicate_password");
		String email = request.queryParams("email");
		if (persistentStorage.checkUserWithUsernameExists(username)) {
			model.put("message_color", "red");
			model.put("message", "User already exists. Choose another username.<br>");
			validated_input = false;
		}
		if (!password.equals(duplicate_password)) {
			model.put("message_color", "red");
			model.put("message", model.get("message") + "Passwords must match.<br>");
			validated_input = false;
		}
		if (password.equals(username) || duplicate_password.equals(username)) {
			model.put("message_color", "red");
			model.put("message", model.get("message") + "Your password cannot be the same as your username.");
			validated_input = false;
		}
		// If the form input was valid, create the user with all permissions enabled.
		if (validated_input) {
			UserPermission[] permissions = {UserPermission.CREATE_TEST, UserPermission.GRADE_TEST};
			persistentStorage.createUser(username, password, email, permissions);
			// Set a green success message and redirect the user back to the login page to log in with their new account.
			request.session().attribute("message_color", "green");
			request.session().attribute("message", "The account <i>" + username + "</i> has been created. Please log in.");
			response.redirect("/login");
		}
		// If the form input was invalid, show the signup form again with the error message.
		return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/signup.vm"));
	};
}
