import controllers.*;
import freemarker.template.Configuration;
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
        port(4567);
        staticFiles.externalLocation("/home/oliver/Projects/Web Projects/markspace/src/main/resources/static"); // TODO: Change to staticFiles.location("/static");
        staticFiles.expireTime(1L);
        enableDebugScreen();
        // Configure the FreeMarker template engine used by the error page. All the pages that I programmed in this project (those under src/main/rsources/templates) use the Velocity template engine.
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_23);
        configuration.setBooleanFormat("truebool,falsebool");
        // Configure web pages.
        PersistentStorage persistentStorage = new DatabaseStorage();
        LoginController loginController = new LoginController(persistentStorage);
        SignupController signupController = new SignupController(persistentStorage);
        TestsController testsController = new TestsController(persistentStorage);
        CreateNewTest1Controller createNewTest1Controller = new CreateNewTest1Controller(persistentStorage);
        CreateNewTest2Controller createNewTest2Controller = new CreateNewTest2Controller(persistentStorage);
        Map<String, PDFRenderer> pdfRendererCache = new HashMap<>();
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
    }
}
