package controllers;

/** @file StudentAnswerFileController.java
 *  Controller for the /student_answer_file page. This page is used to get the raw PDF data of previously-uploaded student answer files.
 *  @see controllers.StudentAnswerFileController
 */

import spark.*;
import storage.PersistentStorage;

/**
 * Controller for the /student_answer_file page, used to get the raw PDF data of previously-uploaded student answer files.
 */
public class StudentAnswerFileController extends Controller {
    /**
     * Create a new StudentAnswerFileController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public StudentAnswerFileController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to /student_answer_file. This page sends back the raw PDF data (content type application/pdf) of a previously-uploaded student answer file.
     * The raw PDF data can be rendered by the user's browser.
     * The GET requests require the following parameters:
     * <ul>
     *     <li>student_answer_file_id - The id number (from the database) of the student answer file to retrieve (the id should be a positive integer).</li>
     * </ul>
     */
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
