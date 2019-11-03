import freemarker.template.Configuration;
import storage.DatabaseStorage;
import storage.PersistentStorage;
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
        RenderTestController renderTestController = new RenderTestController(persistentStorage);
        get("/login", LoginController.serveLoginPageGet);
        post("/login", LoginController.serveLoginPagePost);
        get("/signup", SignupController.serveSignupPageGet);
        post("/signup", SignupController.serveSignupPagePost);
        get("/tests", TestsController.serveTestsPageGet);
        get("/create_new_test_1", createNewTest1Controller.serveCreateNewTest1PageGet);
        post("/create_new_test_1", createNewTest1Controller.serveCreateNewTest1PagePost);
        get("/create_new_test_2", createNewTest2Controller.serveCreateNewTest2PageGet);
        post("/create_new_test_2", createNewTest2Controller.serveCreateNewTest2PagePost);
        get("/render_test", renderTestController.serveRenderTestPageGet);
    }
}
