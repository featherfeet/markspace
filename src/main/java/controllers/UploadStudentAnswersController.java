package controllers;

/** @file UploadStudentAnswersController.java
 *  Controller for the /upload_student_answers page, used to upload scanned PDFs of student answers.
 */

import org.apache.pdfbox.pdmodel.PDDocument;
import spark.*;
import spark.template.velocity.VelocityTemplateEngine;
import storage.*;

import javax.servlet.MultipartConfigElement;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class UploadStudentAnswersController extends Controller {
    private void generateStudentAnswers(int user_id, int test_id, int student_answer_file_id) {
        // Fetch test questions from database.
        TestQuestion[] testQuestions = persistentStorage.getQuestionsByTestId(user_id, test_id);
        // Fetch the number of pages in the student answer file.
        Map<Integer, Integer> studentAnswerFilesNumberOfPages = persistentStorage.getStudentAnswerFilesNumberOfPages(user_id, test_id);
        int student_answer_file_number_of_pages = studentAnswerFilesNumberOfPages.get(student_answer_file_id);
        // Generate student answer objects for the student answer file.
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        // For each page of the current student answer file, find all test questions and create student answer objects from them.
        for (int page = 0; page < student_answer_file_number_of_pages; page++) {
            for (TestQuestion testQuestion : testQuestions) {
                if (testQuestion.getPage() == page) {
                    StudentAnswer studentAnswer = new StudentAnswer(-1, student_answer_file_id, "", testQuestion, "", testQuestion.getPoints(), page);
                    studentAnswers.add(studentAnswer);
                }
            }
        }
        // Save the empty (not yet graded) student answers to the database.
        StudentAnswer[] studentAnswersTemp = new StudentAnswer[studentAnswers.size()];
        studentAnswers.toArray(studentAnswersTemp);
        Integer[] student_answer_ids = persistentStorage.createStudentAnswers(user_id, studentAnswersTemp);
        // Create student answer sets. Each set is the set of all answers by ONE student for a specific test.
        List<Integer> student_answer_set = new ArrayList<>();
        for (int i = 0; i < student_answer_ids.length; i++) {
            student_answer_set.add(student_answer_ids[i]);
            // If we are at the end of a student answer set, add it to the database and clear the set.
            if ((i + 1) % testQuestions.length == 0) {
                Integer[] student_answer_set_temp = new Integer[student_answer_set.size()];
                student_answer_set.toArray(student_answer_set_temp);
                persistentStorage.createStudentAnswerSet(user_id, test_id, student_answer_set_temp);
                student_answer_set.clear();
            }
        }
    }

    /**
     * Create a new UploadStudentAnswersController object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     *
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public UploadStudentAnswersController(PersistentStorage persistentStorage) {
        super(persistentStorage);
    }

    /**
     * Serve GET requests to /upload_student_answers. Shows the form for uploading a scanned PDF of student answers.
     * The GET requests must have the following parameters:
     * <ul>
     *     <li>test_id - The id of the test that the answers are being uploaded for.</li>
     * </ul>
     */
    public Route serveUploadStudentAnswersPageGet = (Request request, Response response) -> {
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
            return "ERROR: This API endpoint requires the test_id parameter.";
        }
        Test test = persistentStorage.getTestById(user_id, test_id);
        if (test == null) {
            return "ERROR: The test you requested either does not exist or is owned by another user.";
        }

        Map<String, Object> model = new HashMap<>();
        model.put("test_name", test.getName());
        model.put("test_pages", persistentStorage.getNumberOfPagesInTestFileById(user_id, test.getAnswersTestFile()));
        model.put("test_id", test_id);

        return new VelocityTemplateEngine().render(new ModelAndView(model, "templates/upload_student_answers.vm"));
    };

    /**
     * Serve POST requests to the /upload_student_answers page, used to submit the form in templates/upload_student_answers.vm.
     * The POST requests must have the following parameters:
     * <ul>
     *     <li>test_id - The id of the test that these answers are being uploaded for.</li>
     *     <li>student_answers_file_upload - The PDF file of scanned student answers being uploaded.</li>
     * </ul>
     */
    public Route serveUploadStudentAnswersPagePost = (Request request, Response response) -> {
        // Set up the Jetty web server to accept file uploads. Max file size of 1 GB. Write all files larger than 1 MB to disk.
        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/tmp", 1000000000, 1000000000, 1000000));
        // Validate the user.
        Boolean valid_user = request.session().attribute("valid_user");
        if (valid_user == null || !valid_user) {
            request.session().attribute("message_color", "red");
            request.session().attribute("message", "You must be logged in to access tests.");
            response.redirect("/login");
            return "";
        }
        int user_id = request.session().attribute("user_id");
        // Get the test id of the test that these answers correspond to.
        int test_id = Integer.parseInt(request.queryParamOrDefault("test_id", "-1"));
        if (test_id == -1) {
            return "ERROR: This API endpoint requires the test_id parameter.";
        }
        Test test = persistentStorage.getTestById(user_id, test_id);
        if (test == null) {
            return "ERROR: The test you requested either does not exist or is owned by another user.";
        }
        // Get raw binary file data.
        byte[] student_answers_file_upload_data = null;
        String student_answers_file_upload_name = null;
        try (InputStream inputStream = request.raw().getPart("student_answers_file_upload").getInputStream()) {
            student_answers_file_upload_name = request.raw().getPart("student_answers_file_upload").getSubmittedFileName();
            student_answers_file_upload_data = new byte[inputStream.available()];
            inputStream.read(student_answers_file_upload_data);
        }
        if (student_answers_file_upload_data == null || student_answers_file_upload_name == null) {
            return "ERROR: You must provide the student_answers_file_upload parameter to this API endpoint.";
        }
        // Save the form data as a new student answer file in the database.
        PDDocument student_answers_file_document = null;
        try {
            student_answers_file_document = PDDocument.load(student_answers_file_upload_data);
        }
        catch (IOException e) {
            return "ERROR: The student answer file uploaded was not valid PDF data.";
        }
        int student_answer_file_id = persistentStorage.createStudentAnswerFile(user_id, test_id, student_answers_file_upload_data, student_answers_file_upload_name, "pdf", student_answers_file_document.getNumberOfPages());
        // Generate empty student answer objects in the database.
        generateStudentAnswers(user_id, test_id, student_answer_file_id);
        // Send the user back to the tests page with a success message.
        request.session().attribute("message_color", "green");
        request.session().attribute("message", "Student answers <b>" + student_answers_file_upload_name + "</b> uploaded successfully to test <b>" + test.getName() + "</b>.");
        response.redirect("/tests");
        return "";
    };
}
