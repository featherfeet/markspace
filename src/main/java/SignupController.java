import spark.*;
import spark.template.velocity.*;
import java.util.*;
import storage.PersistentStorage;
import storage.UserPermission;

public class SignupController {
	private static PersistentStorage persistentStorage;
	
	public SignupController(PersistentStorage persistentStorage) {
		this.persistentStorage = persistentStorage;
	}
	
	public static Route serveSignupPageGet = (Request request, Response response) -> {
		Map<String, Object> model = new HashMap<>();
		model.put("message", "");
		return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/signup.vm"));
	};
	
	public static Route serveSignupPagePost = (Request request, Response response) -> {
		boolean validated_input = true;
		Map<String, Object> model = new HashMap<>();
		model.put("message", "");
		String username = request.queryParams("username");
		String password = request.queryParams("password");
		String duplicate_password = request.queryParams("duplicate_password");
		String email = request.queryParams("email");
		if (!password.equals(duplicate_password)) {
			model.put("message", "Passwords must match.<br>");
			validated_input = false;
		}
		if (password.equals(username) || duplicate_password.equals(username)) {
			model.put("message", model.get("message") + "Your password cannot be the same as your username.");
			validated_input = false;
		}
		if (validated_input) {
			UserPermission[] permissions = {UserPermission.CREATE_TEST, UserPermission.GRADE_TEST};
			persistentStorage.createUser(username, password, email, permissions);
			request.session().attribute("message", "The account <i>" + username + "</i> has been created. Please log in.");
			response.redirect("/login");
		}
		return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/signup.vm"));
	};
}
