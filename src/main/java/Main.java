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
import org.eclipse.jetty.server.Server;
import storage.DatabaseStorage;
import storage.PersistentStorage;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;

import static spark.Spark.*;
import static spark.debug.DebugScreen.*;

public class Main extends JFrame implements ActionListener {
    /**
     * Set this to true to enable live-reload of static resources and the stack-trace debugging screen.
     */
    private static boolean debug_mode = false;

    private JButton start_server_button;
    private JButton stop_server_button;
    private JLabel status_label;
    private JMenuItem aboutMenuItem;
    private JMenuItem licenseMenuItem;

    public Main() {
        initUI();
    }

    private void initUI() {
        // Set up the Swing window.
        setSize(500, 500);
        setTitle("MarkSpace Server Controller");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        // Set up the Swing window contents.
        JLabel markspace_label = new JLabel();
        markspace_label.setText("<html><p style=\"width: 350px;\">This program controls the MarkSpace (C) server, which runs locally on your computer. MarkSpace (C) is a computer-assisted grading system built by Oliver Trevor and Suchin Ravi.</p></html>");
        start_server_button = new JButton("Start Server");
        start_server_button.addActionListener(this);
        stop_server_button = new JButton("Stop Server");
        stop_server_button.addActionListener(this);
        stop_server_button.setEnabled(false);
        status_label = new JLabel();
        status_label.setText("<html><p style=\"width: 350px;\">Server not running. Press the start button.</p></html>");
        setLayout(new GridBagLayout());
        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        add(markspace_label, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        add(start_server_button, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        add(stop_server_button, gridBagConstraints);
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        add(status_label, gridBagConstraints);
        // Set up the menus and license viewer.
        JMenuBar menuBar = new JMenuBar();
        JMenu aboutMenu = new JMenu("About");
        aboutMenu.getAccessibleContext().setAccessibleDescription("Information about the MarkSpace Grading software.");
        menuBar.add(aboutMenu);
        aboutMenuItem = new JMenuItem("About MarkSpace");
        aboutMenuItem.addActionListener(this);
        licenseMenuItem = new JMenuItem("License");
        licenseMenuItem.addActionListener(this);
        aboutMenu.add(aboutMenuItem);
        aboutMenu.add(licenseMenuItem);
        setJMenuBar(menuBar);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == start_server_button) {
            new StartServerThread().start();
        }
        else if (e.getSource() == stop_server_button) {
            new StopServerThread().start();
        }
        else if (e.getSource() == aboutMenuItem) {
            JOptionPane.showMessageDialog(this, "MarkSpace is a computerized grading software package by Oliver Trevor and Suchin Ravi. It was developed in collaboration with  Mr. Joseph Friesen. Go to https://github.com/featherfeet/markspace.git to learn more.");
        }
        else if (e.getSource() == licenseMenuItem) {
            JOptionPane.showMessageDialog(this, "MarkSpace and its source code are distributed under the GNU GPL v3. You should have received a copy of the GPL in the COPYING file of this program. If not, see https://www.gnu.org/licenses/gpl-3.0.txt for the license text.");
        }
    }

    public static void main(String[] args) {
        // Configure the MacOS Swing appearance.
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "MarkSpace Server Controller");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e) {
            System.out.println("Failed to set system look-and-feel on Swing GUI.");
        }
        // Open Swing GUI.
        EventQueue.invokeLater(() -> {
            Main main_obj = new Main();
            main_obj.setVisible(true);
        });
    }

    private class StartServerThread extends Thread {
        public void run() {
            // Print message.
            System.out.println("Starting server...");
            // Show status and disable button.
            start_server_button.setEnabled(false);
            stop_server_button.setEnabled(true);
            status_label.setText("<html><p style=\"width: 350px;\">Server starting...</p></html>");
            // Configure Spark.
            ipAddress("127.0.0.1");
            port(4567); // Serve the application on port 4567.
            if (!debug_mode) {
                staticFiles.location("/static"); // All statically served resources (stylesheets, scripts, etc.) are stored here.
            }
            else {
                staticFiles.externalLocation("/home/oliver/Projects/Web Projects/markspace/src/main/resources/static");
                staticFiles.expireTime(1L);
                enableDebugScreen();
            }
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
            DownloadStudentScoresController downloadStudentScoresController = new DownloadStudentScoresController(persistentStorage);
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
            get("/download_student_scores", downloadStudentScoresController.serveDownloadStudentScoresPageGet);
            // Show status and open browser window.
            status_label.setText("<html><p style=\"width: 350px;\">Server running! Browser should open automatically. If not, go to <a href=\"http://localhost:4567\">http://localhost:4567</a> in any browser.</p></html>");
            try {
                Desktop.getDesktop().browse(new URI("http://localhost:4567"));
            }
            catch (Exception e) {
                System.out.println("Unable to open browser.");
                e.printStackTrace();
            }
            // Print message.
            System.out.println("Server started.");
        }
    }

    private class StopServerThread extends Thread {
        public void run() {
            System.out.println("Stopping server...");
            status_label.setText("Stopping server...");
            spark.Spark.stop();
            status_label.setText("Server stopped. Press start to restart it.");
            start_server_button.setEnabled(true);
            stop_server_button.setEnabled(false);
            System.out.println("Server stopped.");
        }
    }
}
