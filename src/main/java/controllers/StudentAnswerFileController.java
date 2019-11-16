package controllers;

import spark.*;
import storage.PersistentStorage;

public class StudentAnswerFileController extends Controller {
    /**
     * Create a new generic Controller object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public StudentAnswerFileController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    public static Route serveStudentAnswerFilePageGet = (Request request, Response response) -> {
        // Check if the user is valid. If not, send them back to the login page.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get the ID of the requested file.
        int student_answer_file_id = Integer.parseInt(request.queryParamOrDefault("student_answer_file_id", "-1"));
        if (student_answer_file_id == -1) {
            return "ERROR: This API endpoint requires the parameter student_answer_file_id.";
        }
        // Retrieve the raw binary data of the requested file.
        byte[] file_contents = persistentStorage.getStudentAnswerFileById(user_id, student_answer_file_id);
        if (file_contents == null) {
            return "ERROR: The student answer file you requested either does not exist or is owned by another user.";
        }
        // Send the raw binary data back to the client as PDF-type data (to be rendered by the user's browser as a PDF).
        response.type("application/pdf");
        return file_contents;
    };
}
