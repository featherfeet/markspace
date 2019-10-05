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
        // Configure web pages.
        PersistentStorage persistentStorage = new DatabaseStorage();
        LoginController loginController = new LoginController(persistentStorage);
        TestsController testsController = new TestsController(persistentStorage);
        CreateNewTestController createNewTestController = new CreateNewTestController(persistentStorage);
        get("/login", LoginController.serveLoginPageGet);
        post("/login", LoginController.serveLoginPagePost);
        get("/tests", TestsController.serveTestsPageGet);
        get("/create_new_test", createNewTestController.serveCreateNewTestPageGet);
    }
}
