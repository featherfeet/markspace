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

package storage;

/** @file DatabaseStorage.java
 * Implementation of storage.PersistentStorage that uses a JDBC-based MariaDB connection (MySQL). See {@link PersistentStorage} for documentation of most of the methods.
 * @see storage.DatabaseStorage
 * @see storage.PersistentStorage
 */

import security.HashedPassword;
import security.PasswordSecurity;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.sql.*;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * JDBC-based implementation of {@link storage.PersistentStorage} that uses a MariaDB/MySQL database for storage.
 */
public class DatabaseStorage extends PersistentStorage {
    /**
     * A reusable, shared Google Gson object for processing JSON.
     */
    private Gson gson;

    /**
     * The database connection.
     */
    private Connection connection;

    /**
     * A parameterized SQL statement for creating a new user. See {@link DatabaseStorage#createUserSQL}.
     */
    private PreparedStatement createUserStatement;

    /**
     * A parameterized SQL query to check whether user credentials are correct. See {@link DatabaseStorage#validateUserSQL}.
     */
    private PreparedStatement validateUserStatement;

    /**
     * A parameterized SQL query to check if a user with a given username exists. See {@link DatabaseStorage#checkUserWithUsernameExistsSQL}.
     */
    private PreparedStatement checkUserWithUsernameExistsStatement;

    /**
     * A parameterized SQL query to fetch all tests owned by a specific user. See {@link DatabaseStorage#getTestsByUserSQL}.
     */
    private PreparedStatement getTestsByUserStatement;

    /**
     * A parameterized SQl statement to create a new test file. See {@link DatabaseStorage#createTestFileSQL}.
     */
    private PreparedStatement createTestFileStatement;

    /**
     * A parameterized SQL statement to create a new test. See {@link DatabaseStorage#createTestSQL}.
     */
    private PreparedStatement createTestStatement;

    /**
     * A parameterized SQL query to retrieve a test by its id. See {@link DatabaseStorage#getTestByIdSQL}.
     */
    private PreparedStatement getTestByIdStatement;

    /**
     * A parameterized SQL query to retrieve a test file by its id. See {@link DatabaseStorage#getTestFileByIdSQL}.
     */
    private PreparedStatement getTestFileByIdStatement;

    /**
     * A parameterized SQL query to find out how many pages are in a test file by its id. See {@link DatabaseStorage#getNumberOfPagesInTestFileByIdSQL}.
     */
    private PreparedStatement getNumberOfPagesInTestFileByIdStatement;

    /**
     * A parameterized SQL statement to create a new test question. See {@link DatabaseStorage#createQuestionSQL}.
     */
    private PreparedStatement createQuestionStatement;

    /**
     * A parameterized SQL query to retrieve all questions associated with a particular test file by that test file's id. See {@link DatabaseStorage#getQuestionsByTestFileIdSQL}.
     */
    private PreparedStatement getQuestionsByTestFileIdStatement;

    /**
     * A parameterized SQL statement to delete a test by its id. See {@link DatabaseStorage#deleteTestByIdSQL}.
     */
    private PreparedStatement deleteTestByIdStatement;

    /**
     * A parameterized SQL statement to delete a test file by its id. See {@link DatabaseStorage#deleteTestFileByIdSQL}.
     */
    private PreparedStatement deleteTestFileByIdStatement;

    /**
     * A parameterized SQL statement to delete test questions by the id of the test file that they are associated with. See {@link DatabaseStorage#deleteQuestionsByTestFileIdSQL}.
     */
    private PreparedStatement deleteQuestionsByTestFileIdStatement;

    /**
     * A parameterized SQL statement to create a new student answer file. See {@link DatabaseStorage#createStudentAnswerFileSQL}.
     */
    private PreparedStatement createStudentAnswerFileStatement;

    /**
     * A parameterized SQL query to retrieve the ids and names of all student answer files associated with a particular test. See {@link DatabaseStorage#getStudentAnswerFilesSQL}.
     */
    private PreparedStatement getStudentAnswerFilesStatement;

    /**
     * A parameterized SQL query to retrieve a student answer file by its id. See {@link DatabaseStorage#getStudentAnswerFileByIdSQL}.
     */
    private PreparedStatement getStudentAnswerFileByIdStatement;

    /**
     * A parameterized SQL query to retrieve the number of pages in all student answer files attached to a particular test.
     */
    private PreparedStatement getStudentAnswerFilesNumberOfPagesStatement;

    /**
     * A parameterized SQL query to create new student answers.
     */
    private PreparedStatement createStudentAnswerStatement;

    /**
     * A parameterized SQL query to retrieve all student answers attached to a specific student answer file.
     */
    private PreparedStatement getStudentAnswersByStudentAnswerFileIdStatement;

    /**
     * A parameterized SQL query to retrieve a single test question by its id.
     */
    private PreparedStatement getQuestionByIdStatement;

    /**
     * A parameterized SQL query to set the score on a student answer.
     */
    private PreparedStatement scoreStudentAnswerStatement;

    /**
     * A parameterized SQL query to create a student answer set (set of all answers from ONE student for a specific test).
     */
    private PreparedStatement createStudentAnswerSetStatement;

    /**
     * A parameterized SQL query to retrieve a student answer by its id.
     */
    private PreparedStatement getStudentAnswerByIdStatement;

    /**
     * A parameterized SQL query to find the student answer set that contains a particular student answer.
     */
    private PreparedStatement findStudentAnswerSetWithStudentAnswerStatement;

    /**
     * A parameterized SQL query to set the identification (student name or student ID number) on a student answer.
     */
    private PreparedStatement identifyStudentAnswerStatement;

    /**
     * A parameterized SQL query to delete all student answer files associated with a particular test.
     */
    private PreparedStatement deleteStudentAnswerFilesByTestIdStatement;

    /**
     * A parameterized SQL query to delete all student answers attached to a particular student answer file.
     */
    private PreparedStatement deleteStudentAnswersByStudentAnswerFileIdStatement;

    /**
     * A parameterized SQL query to delete all student answer sets associated with a particular test.
     */
    private PreparedStatement deleteStudentAnswerSetsByTestIdStatement;

    private final String createUserSQL = "INSERT INTO users (username, password_hash, password_salt, email, permissions) VALUES (?, ?, ?, ?, ?)";

    private final String validateUserSQL = "SELECT * FROM users WHERE username = ?";
    private final int USERS_TABLE_USER_ID_COLUMN = 1;
    private final int USERS_TABLE_USERNAME_COLUMN = 2;
    private final int USERS_TABLE_PASSWORD_HASH_COLUMN = 3;
    private final int USERS_TABLE_PASSWORD_SALT_COLUMN = 4;
    private final int USERS_TABLE_EMAIL_COLUMN = 5;
    private final int USERS_TABLE_PERMISSIONS_COLUMN = 6;

    private final String checkUserWithUsernameExistsSQL = "SELECT username FROM users WHERE username = ?";

    private final String createTestFileSQL = "INSERT INTO test_files (user_id, test_file, test_file_name, test_file_type, number_of_pages) VALUES (?, ?, ?, ?, ?)";

    private final String createTestSQL = "INSERT INTO tests (user_id, test_name, test_description, blank_test_file, answers_test_file) VALUES (?, ?, ?, ?, ?)";

    private final String getTestsByUserSQL = "SELECT * FROM tests WHERE user_id = ?";
    private final int TESTS_TABLE_TEST_ID_COLUMN = 1;
    private final int TESTS_TABLE_USER_ID_COLUMN = 2;
    private final int TESTS_TABLE_TEST_NAME_COLUMN = 3;
    private final int TESTS_TABLE_TEST_DESCRIPTION_COLUMN = 4;
    private final int TESTS_TABLE_BLANK_TEST_FILE_COLUMN = 5;
    private final int TESTS_TABLE_ANSWERS_TEST_FILE_COLUMN = 6;

    private final String getTestByIdSQL = "SELECT * FROM tests WHERE user_id = ? AND test_id = ?";

    private final String getTestFileByIdSQL = "SELECT * FROM test_files WHERE user_id = ? AND test_file_id = ?";
    private final int TEST_FILES_TABLE_TEST_FILE_ID_COLUMN = 1;
    private final int TEST_FILES_TABLE_USER_ID_COLUMN = 2;
    private final int TEST_FILES_TABLE_TEST_FILE_COLUMN = 3;
    private final int TEST_FILES_TABLE_TEST_FILE_NAME_COLUMN = 4;
    private final int TEST_FILES_TABLE_TEST_FILE_TYPE_COLUMN = 5;
    private final int TEST_FILES_TABLE_NUMBER_OF_PAGES_IN_FILE_COLUMN = 6;

    private final String getNumberOfPagesInTestFileByIdSQL = "SELECT number_of_pages FROM test_files WHERE user_id = ? AND test_file_id = ?";

    private final String createQuestionSQL = "INSERT INTO questions (test_file_id, user_id, information) VALUES (?, ?, ?)";

    private final String getQuestionsByTestFileIdSQL = "SELECT * FROM questions WHERE user_id = ? AND test_file_id = ?";
    private final int QUESTIONS_TABLE_QUESTION_ID_COLUMN = 1;
    private final int QUESTIONS_TABLE_TEST_FILE_ID_COLUMN = 2;
    private final int QUESTIONS_TABLE_USER_ID_COLUMN = 3;
    private final int QUESTIONS_TABLE_INFORMATION_COLUMN = 4;

    private final String deleteTestByIdSQL = "DELETE FROM tests WHERE user_id = ? AND test_id = ?";

    private final String deleteTestFileByIdSQL = "DELETE FROM test_files WHERE user_id = ? AND test_file_id = ?";

    private final String deleteQuestionsByTestFileIdSQL = "DELETE FROM questions WHERE user_id = ? AND test_file_id = ?";

    private final String createStudentAnswerFileSQL = "INSERT INTO student_answer_files (user_id, test_id, student_answer_file, student_answer_file_name, student_answer_file_type, number_of_pages) VALUES (?, ?, ?, ?, ?, ?)";

    private final String getStudentAnswerFilesSQL = "SELECT student_answer_file_id,student_answer_file_name FROM student_answer_files WHERE user_id = ? AND test_id = ?";

    private final String getStudentAnswerFileByIdSQL = "SELECT student_answer_file FROM student_answer_files WHERE user_id = ? AND student_answer_file_id = ?";

    private final String getStudentAnswerFilesNumberOfPagesSQL = "SELECT student_answer_file_id,number_of_pages FROM student_answer_files WHERE user_id = ? AND test_id = ?";

    private final String createStudentAnswerSQL = "INSERT INTO student_answers (user_id, question_id, student_identification, student_answer_file_id, score, points_possible, page) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final String getStudentAnswersByStudentAnswerFileIdSQL = "SELECT * FROM student_answers WHERE user_id = ? AND student_answer_file_id = ?";
    private final int STUDENT_ANSWERS_TABLE_STUDENT_ANSWER_ID_COLUMN = 1;
    private final int STUDENT_ANSWERS_TABLE_USER_ID_COLUMN = 2;
    private final int STUDENT_ANSWERS_TABLE_QUESTION_ID_COLUMN = 3;
    private final int STUDENT_ANSWERS_TABLE_STUDENT_IDENTIFICATION_COLUMN = 4;
    private final int STUDENT_ANSWERS_TABLE_STUDENT_ANSWER_FILE_ID_COLUMN = 5;
    private final int STUDENT_ANSWERS_TABLE_SCORE_COLUMN = 6;
    private final int STUDENT_ANSWERS_TABLE_POINTS_POSSIBLE_COLUMN = 7;
    private final int STUDENT_ANSWERS_TABLE_PAGE_COLUMN = 8;

    private final String getQuestionByIdSQL = "SELECT * FROM questions WHERE user_id = ? AND question_id = ?";

    private final String scoreStudentAnswerSQL = "UPDATE student_answers SET score = ? WHERE user_id = ? AND student_answer_id = ?";

    private final String createStudentAnswerSetSQL = "INSERT INTO student_answer_sets (user_id, test_id, student_answer_ids) VALUES (?, ?, ?)";

    private final String getStudentAnswerByIdSQL = "SELECT * FROM student_answers WHERE user_id = ? AND student_answer_id = ?";

    private final String findStudentAnswerSetWithStudentAnswerSQL = "SELECT * FROM student_answer_sets WHERE user_id = ? AND student_answer_ids LIKE ?";
    private final int STUDENT_ANSWER_SETS_TABLE_STUDENT_ANSWER_SET_ID_COLUMN = 1;
    private final int STUDENT_ANSWER_SETS_TABLE_USER_ID_COLUMN = 2;
    private final int STUDENT_ANSWER_SETS_TABLE_TEST_ID_COLUMN = 3;
    private final int STUDENT_ANSWER_SETS_TABLE_STUDENT_ANSWER_IDS_COLUMN = 4;

    private final String identifyStudentAnswerSQL = "UPDATE student_answers SET student_identification = ? WHERE user_id = ? AND student_answer_id = ?";

    private final String deleteStudentAnswerFilesByTestIdSQL = "DELETE FROM student_answer_files WHERE user_id = ? AND test_id = ?";

    private final String deleteStudentAnswersByStudentAnswerFileIdSQL = "DELETE FROM student_answers WHERE user_id = ? AND student_answer_file_id = ?";

    private final String deleteStudentAnswerSetsByTestIdSQL = "DELETE FROM student_answer_sets WHERE user_id = ? AND test_id = ?";

    /**
     * Initialize the JDBC connection to the database, create the re-usable Google GSON object, and compile all of the parameterized (prepared) SQL queries/statements.
     */
    @Override
    protected synchronized void initializeStorageMethod() {
        try {
            // Create a Google Gson object for processing JSON data.
            gson = new Gson();
            // Create the database connection.
            try {
                Class.forName("org.sqlite.JDBC");
            }
            catch (ClassNotFoundException e) {
                System.out.println("Failed to load SQLite JDBC driver. Database unusable. This is a critical failure; the program will not work.");
                e.printStackTrace();
                return;
            }
            Path databasePath = Paths.get(System.getProperty("user.home"), "markspace.db");
            if (Files.notExists(databasePath)) {
                System.out.println("MarkSpace Database at \"" + databasePath + "\" does not exist. Creating new database at that location.");
                InputStream emptyDatabaseFile = ClassLoader.getSystemClassLoader().getResourceAsStream("markspace.db");
                try {
                    Files.copy(emptyDatabaseFile, databasePath);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            System.out.println("Connecting to DB: " + databasePath);
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath.toString());
            connection.setAutoCommit(true);
            // Create prepared SQL statements for later use.
            createUserStatement = connection.prepareStatement(createUserSQL);
            validateUserStatement = connection.prepareStatement(validateUserSQL);
            checkUserWithUsernameExistsStatement = connection.prepareStatement(checkUserWithUsernameExistsSQL);
            createTestFileStatement = connection.prepareStatement(createTestFileSQL, Statement.RETURN_GENERATED_KEYS);
            createTestStatement = connection.prepareStatement(createTestSQL, Statement.RETURN_GENERATED_KEYS);
            getTestsByUserStatement = connection.prepareStatement(getTestsByUserSQL);
            getTestByIdStatement = connection.prepareStatement(getTestByIdSQL);
            getTestFileByIdStatement = connection.prepareStatement(getTestFileByIdSQL);
            getNumberOfPagesInTestFileByIdStatement = connection.prepareStatement(getNumberOfPagesInTestFileByIdSQL);
            createQuestionStatement = connection.prepareStatement(createQuestionSQL);
            getQuestionsByTestFileIdStatement = connection.prepareStatement(getQuestionsByTestFileIdSQL);
            deleteTestByIdStatement = connection.prepareStatement(deleteTestByIdSQL);
            deleteTestFileByIdStatement = connection.prepareStatement(deleteTestFileByIdSQL);
            deleteQuestionsByTestFileIdStatement = connection.prepareStatement(deleteQuestionsByTestFileIdSQL);
            createStudentAnswerFileStatement = connection.prepareStatement(createStudentAnswerFileSQL, Statement.RETURN_GENERATED_KEYS);
            getStudentAnswerFilesStatement = connection.prepareStatement(getStudentAnswerFilesSQL);
            getStudentAnswerFileByIdStatement = connection.prepareStatement(getStudentAnswerFileByIdSQL);
            getStudentAnswerFilesNumberOfPagesStatement = connection.prepareStatement(getStudentAnswerFilesNumberOfPagesSQL);
            createStudentAnswerStatement = connection.prepareStatement(createStudentAnswerSQL, Statement.RETURN_GENERATED_KEYS);
            getStudentAnswersByStudentAnswerFileIdStatement = connection.prepareStatement(getStudentAnswersByStudentAnswerFileIdSQL);
            getQuestionByIdStatement = connection.prepareStatement(getQuestionByIdSQL);
            scoreStudentAnswerStatement = connection.prepareStatement(scoreStudentAnswerSQL);
            createStudentAnswerSetStatement = connection.prepareStatement(createStudentAnswerSetSQL);
            getStudentAnswerByIdStatement = connection.prepareStatement(getStudentAnswerByIdSQL);
            findStudentAnswerSetWithStudentAnswerStatement = connection.prepareStatement(findStudentAnswerSetWithStudentAnswerSQL);
            identifyStudentAnswerStatement = connection.prepareStatement(identifyStudentAnswerSQL);
            deleteStudentAnswerFilesByTestIdStatement = connection.prepareStatement(deleteStudentAnswerFilesByTestIdSQL);
            deleteStudentAnswersByStudentAnswerFileIdStatement = connection.prepareStatement(deleteStudentAnswersByStudentAnswerFileIdSQL);
            deleteStudentAnswerSetsByTestIdStatement = connection.prepareStatement(deleteStudentAnswerSetsByTestIdSQL);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            connection = null;
        }
    }

    @Override
    public synchronized void createUser(String username, String password, String email, UserPermission[] permissions) {
        if (connection == null) {
            System.out.println("Unable to connect to database.");
            return;
        }
        try {
            createUserStatement.setString(1, username);
            HashedPassword hashedPassword = PasswordSecurity.hashPassword(password);
            createUserStatement.setBytes(2,  hashedPassword.getHash());
            createUserStatement.setBytes(3, hashedPassword.getSalt());
            createUserStatement.setString(4, email);
            String userPermissions = "";
            for (UserPermission userPermission : permissions) {
                userPermissions += userPermission.toString() + ";";
            }
            createUserStatement.setString(5, userPermissions);
            createUserStatement.executeUpdate();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public synchronized int validateUser(String username, String password) {
        if (connection == null) {
            System.out.println("Unable to connect to database.");
            return -1;
        }
        try {
            validateUserStatement.setString(1, username);
            ResultSet validatedUsers = validateUserStatement.executeQuery();
            if (validatedUsers.next()) {
            	HashedPassword correctPassword = new HashedPassword(validatedUsers.getBytes(USERS_TABLE_PASSWORD_SALT_COLUMN), validatedUsers.getBytes(USERS_TABLE_PASSWORD_HASH_COLUMN));
            	if (PasswordSecurity.validatePassword(password, correctPassword)) {
            		return validatedUsers.getInt(USERS_TABLE_USER_ID_COLUMN);
            	}
            }
            return -1;
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return -1;
        }
    }

    @Override
    public synchronized boolean checkUserWithUsernameExists(String username) {
        try {
            checkUserWithUsernameExistsStatement.setString(1, username);
            ResultSet users = checkUserWithUsernameExistsStatement.executeQuery();
            if (users.next()) {
                return true;
            }
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Private helper function used to create a new test file in the database.
     * @param user_id The id of the user who owns the new test file (should be the same as the user who owns the test that the test file is associated with).
     * @param test_file The raw binary data of the test file.
     * @param name The file name of the test file.
     * @param file_type A string like "pdf" or "docx" representing the file type. Right now, this should always be "pdf".
     * @param number_of_pages The number of pages in the test file.
     * @return The id of the created test file.
     */
    private synchronized int createTestFile(int user_id, byte[] test_file, String name, String file_type, int number_of_pages) {
        try {
            createTestFileStatement.setInt(1, user_id);
            createTestFileStatement.setBytes(2, test_file);
            createTestFileStatement.setString(3, name);
            createTestFileStatement.setString(4, file_type);
            createTestFileStatement.setInt(5, number_of_pages);
            createTestFileStatement.executeUpdate();
            ResultSet generated_keys = createTestFileStatement.getGeneratedKeys();
            generated_keys.next();
            return generated_keys.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public synchronized int createTest(int user_id, String name, String description, byte[] blank_test_file, String blank_test_file_name, String blank_test_file_type, byte[] answers_test_file, String answers_test_file_name, String answers_test_file_type) {
        int test_id = -1;
        try {
            PDDocument blank_test_file_document = PDDocument.load(blank_test_file);
            PDDocument answers_test_file_document = PDDocument.load(answers_test_file);
            createTestStatement.setInt(1, user_id);
            createTestStatement.setString(2, name);
            createTestStatement.setString(3, description);
            createTestStatement.setInt(4, createTestFile(user_id, blank_test_file, blank_test_file_name, blank_test_file_type, blank_test_file_document.getNumberOfPages()));
            createTestStatement.setInt(5, createTestFile(user_id, answers_test_file, answers_test_file_name, answers_test_file_type, answers_test_file_document.getNumberOfPages()));
            createTestStatement.executeUpdate();
            blank_test_file_document.close();
            answers_test_file_document.close();
            ResultSet generated_keys = createTestStatement.getGeneratedKeys();
            generated_keys.next();
            test_id = generated_keys.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return test_id;
    }

    @Override
    public synchronized Test[] getTestsByUser(int user_id) {
        try {
            getTestsByUserStatement.setInt(1, user_id);
            ResultSet testsByUser = getTestsByUserStatement.executeQuery();
            List<Test> tests = new ArrayList<>();
            while (testsByUser.next()) {
                int test_id = testsByUser.getInt(TESTS_TABLE_TEST_ID_COLUMN);
                String test_name = testsByUser.getString(TESTS_TABLE_TEST_NAME_COLUMN);
                String test_description = testsByUser.getString(TESTS_TABLE_TEST_DESCRIPTION_COLUMN);
                int blank_test_file_id = testsByUser.getInt(TESTS_TABLE_BLANK_TEST_FILE_COLUMN);
                int answers_test_file_id = testsByUser.getInt(TESTS_TABLE_ANSWERS_TEST_FILE_COLUMN);
                tests.add(new Test(test_id, test_name, test_description, blank_test_file_id, answers_test_file_id));
            }
            return tests.toArray(new Test[tests.size()]);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            return new Test[0];
        }
    }

    @Override
    public synchronized Test getTestById(int user_id, int test_id) {
        Test test = null;
        try {
            getTestByIdStatement.setInt(1, user_id);
            getTestByIdStatement.setInt(2, test_id);
            ResultSet tests = getTestByIdStatement.executeQuery();
            if (tests.next()) {
                int test_id_db = tests.getInt(TESTS_TABLE_TEST_ID_COLUMN);
                String test_name = tests.getString(TESTS_TABLE_TEST_NAME_COLUMN);
                String test_description = tests.getString(TESTS_TABLE_TEST_DESCRIPTION_COLUMN);
                int blank_test_file_id = tests.getInt(TESTS_TABLE_BLANK_TEST_FILE_COLUMN);
                int answers_test_file_id = tests.getInt(TESTS_TABLE_ANSWERS_TEST_FILE_COLUMN);
                test = new Test(test_id_db, test_name, test_description, blank_test_file_id, answers_test_file_id);
            }
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        return test;
    }

    @Override
    public synchronized TestFile getTestFileById(int user_id, int test_file_id) {
        TestFile test_file = null;
        try {
            getTestFileByIdStatement.setInt(1, user_id);
            getTestFileByIdStatement.setInt(2, test_file_id);
            ResultSet test_files = getTestFileByIdStatement.executeQuery();
            if (test_files.next()) {
                byte[] data = test_files.getBytes(TEST_FILES_TABLE_TEST_FILE_COLUMN);
                String name = test_files.getString(TEST_FILES_TABLE_TEST_FILE_NAME_COLUMN);
                String type = test_files.getString(TEST_FILES_TABLE_TEST_FILE_TYPE_COLUMN);
                int number_of_pages = test_files.getInt(TEST_FILES_TABLE_NUMBER_OF_PAGES_IN_FILE_COLUMN);
                test_file = new TestFile(test_file_id, data, name, type, number_of_pages);
            }
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        return test_file;
    }

    @Override
    public synchronized int getNumberOfPagesInTestFileById(int user_id, int test_file_id) {
        int number_of_pages = -1;
        try {
            getNumberOfPagesInTestFileByIdStatement.setInt(1, user_id);
            getNumberOfPagesInTestFileByIdStatement.setInt(2, test_file_id);
            ResultSet resultSet = getNumberOfPagesInTestFileByIdStatement.executeQuery();
            if (resultSet.next()) {
                number_of_pages = resultSet.getInt(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return number_of_pages;
    }

    @Override
    public synchronized void createQuestions(int test_file_id, int user_id, TestQuestion[] questions) {
        try {
            int batch_count = 0;
            for (TestQuestion question : questions) {
                createQuestionStatement.setInt(1, test_file_id);
                createQuestionStatement.setInt(2, user_id);
                createQuestionStatement.setString(3, gson.toJson(question));
                createQuestionStatement.addBatch();
                batch_count++;
                if (batch_count % 100 == 0 || batch_count == questions.length) {
                    createQuestionStatement.executeBatch();
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized TestQuestion[] getQuestionsByTestFileId(int user_id, int test_file_id) {
        try {
            getQuestionsByTestFileIdStatement.setInt(1, user_id);
            getQuestionsByTestFileIdStatement.setInt(2, test_file_id);
            ResultSet resultSet = getQuestionsByTestFileIdStatement.executeQuery();
            List<TestQuestion> testQuestions = new ArrayList<>();
            while (resultSet.next()) {
                String raw_json = resultSet.getString(QUESTIONS_TABLE_INFORMATION_COLUMN);
                TestQuestion testQuestion = gson.fromJson(raw_json, TestQuestion.class);
                int test_question_id = resultSet.getInt(QUESTIONS_TABLE_QUESTION_ID_COLUMN);
                testQuestion.setTestQuestionId(test_question_id);
                testQuestions.add(testQuestion);
            }
            return testQuestions.toArray(new TestQuestion[testQuestions.size()]);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return new TestQuestion[0];
    }

    @Override
    public synchronized TestQuestion[] getQuestionsByTestId(int user_id, int test_id) {
        Test test = getTestById(user_id, test_id);
        return getQuestionsByTestFileId(user_id, test.getAnswersTestFile());
    }

    /**
     * A private helper function to delete a test file by its id.
     * @param user_id The user id of the user who owns the test file.
     * @param test_file_id The id of the test file to be deleted.
     */
    private synchronized void deleteTestFileById(int user_id, int test_file_id) {
        try {
            deleteTestFileByIdStatement.setInt(1, user_id);
            deleteTestFileByIdStatement.setInt(2, test_file_id);
            deleteTestFileByIdStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A private helper function to delete all student answer files attached to a particular test.
     * @param user_id The user id of the user who owns the test.
     * @param test_id The test id of the test for which student answer files should be deleted.
     */
    private synchronized void deleteStudentAnswerFilesByTestId(int user_id, int test_id) {
        try {
            deleteStudentAnswerFilesByTestIdStatement.setInt(1, user_id);
            deleteStudentAnswerFilesByTestIdStatement.setInt(2, test_id);
            deleteStudentAnswerFilesByTestIdStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A private helper function to delete all student answers attached to a particular student answer file.
     * @param user_id The user id of the user who owns the student answers.
     * @param student_answer_file_id The id of the student answer file.
     */
    private synchronized void deleteStudentAnswersByStudentAnswerFileId(int user_id, int student_answer_file_id) {
        try {
            deleteStudentAnswersByStudentAnswerFileIdStatement.setInt(1, user_id);
            deleteStudentAnswersByStudentAnswerFileIdStatement.setInt(2, student_answer_file_id);
            deleteStudentAnswersByStudentAnswerFileIdStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * A private helper function to delete all student answer sets associated with a test.
     * @param user_id The user id of the user who owns the test and student answer sets.
     * @param test_id The test whose student answer sets are being deleted.
     */
    private synchronized void deleteStudentAnswerSetsByTestId(int user_id, int test_id) {
        try {
            deleteStudentAnswerSetsByTestIdStatement.setInt(1, user_id);
            deleteStudentAnswerSetsByTestIdStatement.setInt(2, test_id);
            deleteStudentAnswerSetsByTestIdStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void deleteTestById(int user_id, int test_id) {
        try {
            Test test_to_delete = getTestById(user_id, test_id);
            if (test_to_delete != null) {
                // Delete the test files associated with this test.
                deleteTestFileById(user_id, test_to_delete.getAnswersTestFile());
                deleteTestFileById(user_id, test_to_delete.getBlankTestFile());
                // Delete this test.
                deleteTestByIdStatement.setInt(1, user_id);
                deleteTestByIdStatement.setInt(2, test_id);
                deleteTestByIdStatement.executeUpdate();
                // Delete all test questions associated with this test.
                deleteQuestionsByTestFileIdStatement.setInt(1, user_id);
                deleteQuestionsByTestFileIdStatement.setInt(2, test_to_delete.getAnswersTestFile());
                deleteQuestionsByTestFileIdStatement.executeUpdate();
                // Delete all student answer files associated with this test.
                Set<Integer> student_answer_file_ids = getStudentAnswerFilesNumberOfPages(user_id, test_id).keySet();
                deleteStudentAnswerFilesByTestId(user_id, test_id);
                // Delete all student answers associated with this test.
                for (Integer student_answer_file_id : student_answer_file_ids) {
                    deleteStudentAnswersByStudentAnswerFileId(user_id, student_answer_file_id);
                }
                // Delete all student answer sets associated with this test.
                deleteStudentAnswerSetsByTestId(user_id, test_id);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized int createStudentAnswerFile(int user_id, int test_id, byte[] student_answer_file, String student_answer_file_name, String student_answer_file_type, int number_of_pages) {
        int student_answer_file_id = -1;
        try {
            createStudentAnswerFileStatement.setInt(1, user_id);
            createStudentAnswerFileStatement.setInt(2, test_id);
            createStudentAnswerFileStatement.setBytes(3, student_answer_file);
            createStudentAnswerFileStatement.setString(4, student_answer_file_name);
            createStudentAnswerFileStatement.setString(5, student_answer_file_type);
            createStudentAnswerFileStatement.setInt(6, number_of_pages);
            createStudentAnswerFileStatement.executeUpdate();
            ResultSet resultSet = createStudentAnswerFileStatement.getGeneratedKeys();
            resultSet.next();
            student_answer_file_id = resultSet.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return student_answer_file_id;
    }

    @Override
    public synchronized Map<Integer, String> getStudentAnswerFilesByTestId(int user_id, int test_id) {
        Map<Integer, String> student_answer_files = new HashMap<>();
        try {
            getStudentAnswerFilesStatement.setInt(1, user_id);
            getStudentAnswerFilesStatement.setInt(2, test_id);
            ResultSet resultSet = getStudentAnswerFilesStatement.executeQuery();
            while (resultSet.next()) {
                int student_answer_file_id = resultSet.getInt(1);
                String student_answer_file_name = resultSet.getString(2);
                student_answer_files.put(student_answer_file_id, student_answer_file_name);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return student_answer_files;
    }

    @Override
    public synchronized byte[] getStudentAnswerFileById(int user_id, int student_answer_file_id) {
        byte[] file_contents = null;
        try {
            getStudentAnswerFileByIdStatement.setInt(1, user_id);
            getStudentAnswerFileByIdStatement.setInt(2, student_answer_file_id);
            ResultSet resultSet = getStudentAnswerFileByIdStatement.executeQuery();
            if (resultSet.next()) {
                file_contents = resultSet.getBytes(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return file_contents;
    }

    public synchronized Map<Integer, Integer> getStudentAnswerFilesNumberOfPages(int user_id, int test_id) {
        Map<Integer, Integer> student_answer_files_number_of_pages = new HashMap<>();
        try {
            getStudentAnswerFilesNumberOfPagesStatement.setInt(1, user_id);
            getStudentAnswerFilesNumberOfPagesStatement.setInt(2, test_id);
            ResultSet resultSet = getStudentAnswerFilesNumberOfPagesStatement.executeQuery();
            while (resultSet.next()) {
                int student_answer_file_id = resultSet.getInt(1);
                int student_answer_file_pages = resultSet.getInt(2);
                student_answer_files_number_of_pages.put(student_answer_file_id, student_answer_file_pages);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return student_answer_files_number_of_pages;
    }

    public synchronized Integer[] createStudentAnswers(int user_id, StudentAnswer[] student_answers) {
        List<Integer> student_answer_ids = new ArrayList<>();
        try {
            for (StudentAnswer student_answer : student_answers) {
                createStudentAnswerStatement.setInt(1, user_id);
                createStudentAnswerStatement.setInt(2, student_answer.getTestQuestion().getTestQuestionId());
                createStudentAnswerStatement.setString(3, student_answer.getStudentIdentification());
                createStudentAnswerStatement.setInt(4, student_answer.getStudentAnswerFileId());
                createStudentAnswerStatement.setString(5, student_answer.getScore());
                createStudentAnswerStatement.setString(6, student_answer.getPointsPossible());
                createStudentAnswerStatement.setInt(7, student_answer.getPage());
                createStudentAnswerStatement.executeUpdate();
                // Retrieve the ID of the added student answer.
                ResultSet resultSet = createStudentAnswerStatement.getGeneratedKeys();
                while (resultSet.next()) {
                    student_answer_ids.add(resultSet.getInt(1));
                }
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        Integer[] student_answer_ids_temp = new Integer[student_answer_ids.size()];
        student_answer_ids.toArray(student_answer_ids_temp);
        return student_answer_ids_temp;
    }

    public synchronized TestQuestion getQuestionById(int user_id, int question_id) {
        TestQuestion testQuestion = null;
        try {
            getQuestionByIdStatement.setInt(1, user_id);
            getQuestionByIdStatement.setInt(2, question_id);
            ResultSet resultSet = getQuestionByIdStatement.executeQuery();
            resultSet.next();
            String raw_json = resultSet.getString(QUESTIONS_TABLE_INFORMATION_COLUMN);
            testQuestion = gson.fromJson(raw_json, TestQuestion.class);
            testQuestion.setTestQuestionId(question_id);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return testQuestion;
    }

    public synchronized List<StudentAnswer> getStudentAnswersByStudentAnswerFileId(int user_id, int student_answer_file_id) {
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        try {
            getStudentAnswersByStudentAnswerFileIdStatement.setInt(1, user_id);
            getStudentAnswersByStudentAnswerFileIdStatement.setInt(2, student_answer_file_id);
            ResultSet resultSet = getStudentAnswersByStudentAnswerFileIdStatement.executeQuery();
            while (resultSet.next()) {
                int student_answer_id = resultSet.getInt(STUDENT_ANSWERS_TABLE_STUDENT_ANSWER_ID_COLUMN);
                int question_id = resultSet.getInt(STUDENT_ANSWERS_TABLE_QUESTION_ID_COLUMN);
                String student_identification = resultSet.getString(STUDENT_ANSWERS_TABLE_STUDENT_IDENTIFICATION_COLUMN);
                String score = resultSet.getString(STUDENT_ANSWERS_TABLE_SCORE_COLUMN);
                String points_possible = resultSet.getString(STUDENT_ANSWERS_TABLE_POINTS_POSSIBLE_COLUMN);
                int page = resultSet.getInt(STUDENT_ANSWERS_TABLE_PAGE_COLUMN);
                StudentAnswer studentAnswer = new StudentAnswer(student_answer_id, student_answer_file_id, student_identification, getQuestionById(user_id, question_id), score, points_possible, page);
                studentAnswers.add(studentAnswer);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return studentAnswers;
    }

    public synchronized void scoreStudentAnswer(int user_id, int student_answer_id, String score) {
        try {
            scoreStudentAnswerStatement.setString(1, score);
            scoreStudentAnswerStatement.setInt(2, user_id);
            scoreStudentAnswerStatement.setInt(3, student_answer_id);
            scoreStudentAnswerStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized void createStudentAnswerSet(int user_id, int test_id, Integer[] student_answer_ids) {
        try {
            createStudentAnswerSetStatement.setInt(1, user_id);
            createStudentAnswerSetStatement.setInt(2, test_id);
            String student_answer_ids_string = ",";
            for (int student_answer_id : student_answer_ids) {
                student_answer_ids_string += student_answer_id + ",";
            }
            createStudentAnswerSetStatement.setString(3, student_answer_ids_string);
            createStudentAnswerSetStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized StudentAnswer getStudentAnswerById(int user_id, int student_answer_id) {
        StudentAnswer studentAnswer = null;
        try {
            getStudentAnswerByIdStatement.setInt(1, user_id);
            getStudentAnswerByIdStatement.setInt(2, student_answer_id);
            ResultSet resultSet = getStudentAnswerByIdStatement.executeQuery();
            resultSet.next();
            int student_answer_file_id = resultSet.getInt(STUDENT_ANSWERS_TABLE_STUDENT_ANSWER_FILE_ID_COLUMN);
            int question_id = resultSet.getInt(STUDENT_ANSWERS_TABLE_QUESTION_ID_COLUMN);
            String student_identification = resultSet.getString(STUDENT_ANSWERS_TABLE_STUDENT_IDENTIFICATION_COLUMN);
            String score = resultSet.getString(STUDENT_ANSWERS_TABLE_SCORE_COLUMN);
            String points_possible = resultSet.getString(STUDENT_ANSWERS_TABLE_POINTS_POSSIBLE_COLUMN);
            int page = resultSet.getInt(STUDENT_ANSWERS_TABLE_PAGE_COLUMN);
            studentAnswer = new StudentAnswer(student_answer_id, student_answer_file_id, student_identification, getQuestionById(user_id, question_id), score, points_possible, page);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return studentAnswer;
    }

    public synchronized StudentAnswerSet findStudentAnswerSetWithStudentAnswer(int user_id, int student_answer_id) {
        StudentAnswerSet studentAnswerSet = null;
        try {
            findStudentAnswerSetWithStudentAnswerStatement.setInt(1, user_id);
            findStudentAnswerSetWithStudentAnswerStatement.setString(2, "%," + student_answer_id + ",%");
            ResultSet resultSet = findStudentAnswerSetWithStudentAnswerStatement.executeQuery();
            resultSet.next();
            int student_answer_set_id = resultSet.getInt(STUDENT_ANSWER_SETS_TABLE_STUDENT_ANSWER_SET_ID_COLUMN);
            int test_id = resultSet.getInt(STUDENT_ANSWER_SETS_TABLE_TEST_ID_COLUMN);
            String student_answer_ids_raw = resultSet.getString(STUDENT_ANSWER_SETS_TABLE_STUDENT_ANSWER_IDS_COLUMN);
            String[] student_answer_ids_raw_split = student_answer_ids_raw.split(",");
            List<Integer> student_answer_ids = new ArrayList<>();
            for (String student_answer_id_raw : student_answer_ids_raw_split) {
                if (student_answer_id_raw.length() > 0) {
                    student_answer_ids.add(Integer.parseInt(student_answer_id_raw));
                }
            }
            Integer[] student_answer_ids_array = new Integer[student_answer_ids.size()];
            student_answer_ids.toArray(student_answer_ids_array);
            studentAnswerSet = new StudentAnswerSet(student_answer_set_id, user_id, test_id, student_answer_ids_array);
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return studentAnswerSet;
    }

    public synchronized void identifyStudentAnswer(int user_id, int student_answer_id, String student_identification) {
        try {
            identifyStudentAnswerStatement.setString(1, student_identification);
            identifyStudentAnswerStatement.setInt(2, user_id);
            identifyStudentAnswerStatement.setInt(3, student_answer_id);
            identifyStudentAnswerStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized StudentAnswer[] findAllStudentsWhoTookTest(int user_id, int test_id) {
        // Find all student answer files for this test.
        Map<Integer, String> studentAnswerFiles = this.getStudentAnswerFilesByTestId(user_id, test_id);
        Set<Integer> studentAnswerFileIds = studentAnswerFiles.keySet();
        // Find all student answers for the student answer files for this test.
        List<StudentAnswer> studentAnswers = new ArrayList<>();
        for (int student_answer_file_id : studentAnswerFileIds) {
            studentAnswers.addAll(this.getStudentAnswersByStudentAnswerFileId(user_id, student_answer_file_id));
        }
        // Filter out student answers that aren't for NAME questions.
        List<StudentAnswer> identificationStudentAnswers = new ArrayList<>();
        for (StudentAnswer studentAnswer : studentAnswers) {
            if (studentAnswer.getTestQuestion().isIdentificationQuestion()) {
                identificationStudentAnswers.add(studentAnswer);
            }
        }
        // Convert identificationStudentAnswers to an array.
        StudentAnswer[] identificationStudentAnswersTemp = new StudentAnswer[identificationStudentAnswers.size()];
        identificationStudentAnswers.toArray(identificationStudentAnswersTemp);
        return identificationStudentAnswersTemp;
    }
}
