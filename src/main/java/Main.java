/**
 * @file Main.java
 * This file is the entry point for the software. It sets up the Jetty web server, Spark routes, and any other configuration.
 * The software has an MVC (Model-View-Controller) architecture. The views, which render the actual user interface, are Velocity template engine files in src/main/resources/templates/.
 * The controllers, which have all of the "business logic" that interfaces the database, are classes under src/main/java/controllers. They contain static Route functions that define behaviors for GET/POST requests.
 * The models are HashMap<String, Object> objects created inside controllers and passed to views. They contain any data that the Velocity template needs to render itself.
 * All data is backed by the PersistentStorage. The PersistentStorage is, at present, implemented only by the DatabaseStorage, which is a JDBC-based class for accessing a MariaDB (MySQL) database using prepared queries.
 */

import controllers.*;
import org.apache.pdfbox.rendering.PDFRenderer;
import storage.DatabaseStorage;
import storage.PersistentStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

public class Main {
    public static void main(String[] args) {
        // Configure Spark.
        port(4567); // Serve the application on port 4567.
        // staticFiles.location("/static"); // All statically served resources (stylesheets, scripts, etc.) are stored here.
        staticFiles.externalLocation("/home/oliver/Projects/Web Projects/markspace/src/main/resources/static"); // TODO: Change to staticFiles.location("/static");
        staticFiles.expireTime(1L);
        enableDebugScreen();
        // Configure web pages.
        PersistentStorage persistentStorage = new DatabaseStorage(); // Create a DatabaseStorage object to interface the database. Only one should ever be instantiated; it is shared between all of the controllers.
        // Instantiate objects for all of the controllers. Although the controllers' methods are static, they MUST be constructed in order to pass them a shared PersistentStorage object.
        LoginController loginController = new LoginController(persistentStorage);
        SignupController signupController = new SignupController(persistentStorage);
        TestsController testsController = new TestsController(persistentStorage);
        CreateNewTest1Controller createNewTest1Controller = new CreateNewTest1Controller(persistentStorage);
        CreateNewTest2Controller createNewTest2Controller = new CreateNewTest2Controller(persistentStorage);
        /* All of the controllers that do server-side rendering of PDFs share a cache of pre-loaded PDFRenderer objects,
        indexed by which PDF they are associated with. */
        Map<String, PDFRenderer> pdfRendererCache = new HashMap<>();
        /* Since Jetty runs request handlers concurrently, a client requesting every page of a 50-page PDF to be
        rendered would cause the server to create 50 of the same PDFRenderer object (since the PDFRenderer objects
        only get placed into the cache after a request completes). To avoid this, a global Set of all PDFRenderer
        objects in the process of being created is maintained here. While one request handler is creating a PDFRenderer,
        it puts that PDFRenderer's key into this Set. This causes any other request handlers looking for the same
        PDFRenderer to wait until it appears in the cache, thus reducing the creation of superfluous PDFRenderers. */
        Set<String> renderersBeingCreated = new HashSet<>();
        RenderTestController renderTestController = new RenderTestController(persistentStorage, pdfRendererCache, renderersBeingCreated);
        TestController testController = new TestController(persistentStorage);
        GetQuestionsController getQuestionsController = new GetQuestionsController(persistentStorage);
        DeleteTestController deleteTestController = new DeleteTestController(persistentStorage);
        SignoutController signoutController = new SignoutController(persistentStorage);
        IndexController indexController = new IndexController(persistentStorage);
        UploadStudentAnswersController uploadStudentAnswersController = new UploadStudentAnswersController(persistentStorage);
        StudentAnswerFileController studentAnswerFileController = new StudentAnswerFileController(persistentStorage);
        RenderQuestionController renderQuestionController = new RenderQuestionController(persistentStorage, pdfRendererCache, renderersBeingCreated);
        GradeTestController gradeTestController = new GradeTestController(persistentStorage);
        RenderStudentAnswerController renderStudentAnswerController = new RenderStudentAnswerController(persistentStorage);
        GetStudentAnswersForTestController getStudentAnswersForTestController = new GetStudentAnswersForTestController(persistentStorage);
        ScoreStudentAnswerController scoreStudentAnswerController = new ScoreStudentAnswerController(persistentStorage);
        ViewStudentScoresController viewStudentScoresController = new ViewStudentScoresController(persistentStorage);
        // Configure which types of requests and what URLs correspond to each request handler.
        get("/", indexController.serveIndexPageGet);
        get("/index", indexController.serveIndexPageGet);
        get("/login", loginController.serveLoginPageGet);
        post("/login", loginController.serveLoginPagePost);
        get("/signout", signoutController.serveSignoutPageGet);
        get("/signup", signupController.serveSignupPageGet);
        post("/signup", signupController.serveSignupPagePost);
        get("/tests", testsController.serveTestsPageGet);
        get("/create_new_test_1", createNewTest1Controller.serveCreateNewTest1PageGet);
        post("/create_new_test_1", createNewTest1Controller.serveCreateNewTest1PagePost);
        get("/create_new_test_2", createNewTest2Controller.serveCreateNewTest2PageGet);
        post("/create_new_test_2", createNewTest2Controller.serveCreateNewTest2PagePost);
        get("/render_test", renderTestController.serveRenderTestPageGet);
        get("/test", testController.serveTestPageGet);
        get("/get_questions", getQuestionsController.serveGetQuestionsPageGet);
        get("/delete_test", deleteTestController.serveDeleteTestPageGet);
        get("/upload_student_answers", uploadStudentAnswersController.serveUploadStudentAnswersPageGet);
        post("/upload_student_answers", uploadStudentAnswersController.serveUploadStudentAnswersPagePost);
        get("/student_answer_file", studentAnswerFileController.serveStudentAnswerFilePageGet);
        get("/render_question", renderQuestionController.serveRenderQuestionPageGet);
        get("/grade_test", gradeTestController.serveGradeTestPageGet);
        get("/render_student_answer", renderStudentAnswerController.serveRenderStudentAnswerPageGet);
        get("/get_student_answers_for_test", getStudentAnswersForTestController.serveGetStudentAnswersForTestPageGet);
        post("/score_student_answer", scoreStudentAnswerController.serveScoreStudentAnswerPagePost);
        get("/view_student_scores", viewStudentScoresController.serveViewStudentScoresPageGet);
    }
}
