package storage;

/** @file DatabaseStorage.java
 * Implementation of storage.PersistentStorage that uses a JDBC-based MariaDB connection (MySQL). See {@link PersistentStorage} for documentation of most of the methods.
 * @see storage.DatabaseStorage
 * @see storage.PersistentStorage
 */

import security.HashedPassword;
import security.PasswordSecurity;
import javax.sql.rowset.serial.SerialBlob;

import com.google.gson.Gson;

import java.io.IOException;
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

    private final String createUserSQL = "INSERT INTO users (user_id, username, password_hash, password_salt, email, permissions) VALUES (default, ?, ?, ?, ?, ?)";

    private final String validateUserSQL = "SELECT * FROM users WHERE username = ?";
    private final int USERS_TABLE_USER_ID_COLUMN = 1;
    private final int USERS_TABLE_USERNAME_COLUMN = 2;
    private final int USERS_TABLE_PASSWORD_HASH_COLUMN = 3;
    private final int USERS_TABLE_PASSWORD_SALT_COLUMN = 4;
    private final int USERS_TABLE_EMAIL_COLUMN = 5;
    private final int USERS_TABLE_PERMISSIONS_COLUMN = 6;

    private final String checkUserWithUsernameExistsSQL = "SELECT username FROM users WHERE username = ?";

    private final String createTestFileSQL = "INSERT INTO test_files (test_file_id, user_id, test_file, test_file_name, test_file_type, number_of_pages) VALUES (default, ?, ?, ?, ?, ?)";

    private final String createTestSQL = "INSERT INTO tests (test_id, user_id, test_name, test_description, blank_test_file, answers_test_file) VALUES (default, ?, ?, ?, ?, ?)";

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

    private final String createQuestionSQL = "INSERT INTO questions (question_id, test_file_id, user_id, information) VALUES (default, ?, ?, ?)";

    private final String getQuestionsByTestFileIdSQL = "SELECT * FROM questions WHERE user_id = ? AND test_file_id = ?";
    private final int QUESTIONS_TABLE_QUESTION_ID_COLUMN = 1;
    private final int QUESTIONS_TABLE_TEST_FILE_ID_COLUMN = 2;
    private final int QUESTIONS_TABLE_USER_ID_COLUMN = 3;
    private final int QUESTIONS_TABLE_INFORMATION_COLUMN = 4;

    private final String deleteTestByIdSQL = "DELETE FROM tests WHERE user_id = ? AND test_id = ?";

    private final String deleteTestFileByIdSQL = "DELETE FROM test_files WHERE user_id = ? AND test_file_id = ?";

    private final String deleteQuestionsByTestFileIdSQL = "DELETE FROM questions WHERE user_id = ? AND test_file_id = ?";

    private final String createStudentAnswerFileSQL = "INSERT INTO student_answer_files (student_answer_file_id, user_id, test_id, student_answer_file, student_answer_file_name, student_answer_file_type, number_of_pages) VALUES (default, ?, ?, ?, ?, ?, ?)";

    private final String getStudentAnswerFilesSQL = "SELECT student_answer_file_id,student_answer_file_name FROM student_answer_files WHERE user_id = ? AND test_id = ?";

    private final String getStudentAnswerFileByIdSQL = "SELECT student_answer_file FROM student_answer_files WHERE user_id = ? AND student_answer_file_id = ?";

    /**
     * Initialize the JDBC connection to the database, create the re-usable Google GSON object, and compile all of the parameterized (prepared) SQL queries/statements.
     */
    @Override
    protected void initializeStorageMethod() {
        try {
            gson = new Gson();
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/markspace", "root", "GradingIsFunForYouXDXD42069!!!");
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
            createStudentAnswerFileStatement = connection.prepareStatement(createStudentAnswerFileSQL);
            getStudentAnswerFilesStatement = connection.prepareStatement(getStudentAnswerFilesSQL);
            getStudentAnswerFileByIdStatement = connection.prepareStatement(getStudentAnswerFileByIdSQL);
        }
        catch (SQLException exception) {
            exception.printStackTrace();
            connection = null;
        }
    }

    @Override
    public void createUser(String username, String password, String email, UserPermission[] permissions) {
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
            connection.commit();
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public int validateUser(String username, String password) {
        if (connection == null) {
            System.out.println("Unable to connect to database.");
            return -1;
        }
        try {
            validateUserStatement.setString(1, username);
            ResultSet validatedUsers = validateUserStatement.executeQuery();
            if (validatedUsers.first()) {
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
    public boolean checkUserWithUsernameExists(String username) {
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
    private int createTestFile(int user_id, byte[] test_file, String name, String file_type, int number_of_pages) {
        try {
            SerialBlob test_file_blob = new SerialBlob(test_file);
            createTestFileStatement.setInt(1, user_id);
            createTestFileStatement.setBlob(2, test_file_blob);
            createTestFileStatement.setString(3, name);
            createTestFileStatement.setString(4, file_type);
            createTestFileStatement.setInt(5, number_of_pages);
            createTestFileStatement.executeUpdate();
            ResultSet generated_keys = createTestFileStatement.getGeneratedKeys();
            generated_keys.first();
            return generated_keys.getInt(1);
        }
        catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public int createTest(int user_id, String name, String description, byte[] blank_test_file, String blank_test_file_name, String blank_test_file_type, byte[] answers_test_file, String answers_test_file_name, String answers_test_file_type) {
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
            generated_keys.first();
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
    public Test[] getTestsByUser(int user_id) {
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
    public Test getTestById(int user_id, int test_id) {
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
    public TestFile getTestFileById(int user_id, int test_file_id) {
        TestFile test_file = null;
        try {
            getTestFileByIdStatement.setInt(1, user_id);
            getTestFileByIdStatement.setInt(2, test_file_id);
            ResultSet test_files = getTestFileByIdStatement.executeQuery();
            if (test_files.first()) {
                byte[] data = test_files.getBytes(TEST_FILES_TABLE_TEST_FILE_COLUMN);
                String name = test_files.getString(TEST_FILES_TABLE_TEST_FILE_NAME_COLUMN);
                String type = test_files.getString(TEST_FILES_TABLE_TEST_FILE_TYPE_COLUMN);
                test_file = new TestFile(test_file_id, data, name, type);
            }
        }
        catch (SQLException exception) {
            exception.printStackTrace();
        }
        return test_file;
    }

    @Override
    public int getNumberOfPagesInTestFileById(int user_id, int test_file_id) {
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
    public void createQuestions(int test_file_id, int user_id, TestQuestion[] questions) {
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
    public TestQuestion[] getQuestionsByTestFileId(int user_id, int test_file_id) {
        try {
            getQuestionsByTestFileIdStatement.setInt(1, user_id);
            getQuestionsByTestFileIdStatement.setInt(2, test_file_id);
            ResultSet resultSet = getQuestionsByTestFileIdStatement.executeQuery();
            List<TestQuestion> testQuestions = new ArrayList<>();
            while (resultSet.next()) {
                String raw_json = resultSet.getString(QUESTIONS_TABLE_INFORMATION_COLUMN);
                TestQuestion testQuestion = gson.fromJson(raw_json, TestQuestion.class);
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
    public TestQuestion[] getQuestionsByTestId(int user_id, int test_id) {
        Test test = getTestById(user_id, test_id);
        return getQuestionsByTestFileId(user_id, test.getAnswersTestFile());
    }

    /**
     * A private helper function to delete a test file by its id.
     * @param user_id The user id of the user who owns the test file.
     * @param test_file_id The id of the test file to be deleted.
     */
    private void deleteTestFileById(int user_id, int test_file_id) {
        try {
            deleteTestFileByIdStatement.setInt(1, user_id);
            deleteTestFileByIdStatement.setInt(2, test_file_id);
            deleteTestFileByIdStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteTestById(int user_id, int test_id) {
        try {
            Test test_to_delete = getTestById(user_id, test_id);
            if (test_to_delete != null) {
                deleteTestFileById(user_id, test_to_delete.getAnswersTestFile());
                deleteTestFileById(user_id, test_to_delete.getBlankTestFile());
                deleteTestByIdStatement.setInt(1, user_id);
                deleteTestByIdStatement.setInt(2, test_id);
                deleteTestByIdStatement.executeUpdate();
                deleteQuestionsByTestFileIdStatement.setInt(1, user_id);
                deleteQuestionsByTestFileIdStatement.setInt(2, test_to_delete.getAnswersTestFile());
                deleteQuestionsByTestFileIdStatement.executeUpdate();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createStudentAnswerFile(int user_id, int test_id, byte[] student_answer_file, String student_answer_file_name, String student_answer_file_type, int number_of_pages) {
        try {
            createStudentAnswerFileStatement.setInt(1, user_id);
            createStudentAnswerFileStatement.setInt(2, test_id);
            SerialBlob student_answer_file_blob = new SerialBlob(student_answer_file);
            createStudentAnswerFileStatement.setBlob(3, student_answer_file_blob);
            createStudentAnswerFileStatement.setString(4, student_answer_file_name);
            createStudentAnswerFileStatement.setString(5, student_answer_file_type);
            createStudentAnswerFileStatement.setInt(6, number_of_pages);
            createStudentAnswerFileStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Map<Integer, String> getStudentAnswerFilesByTestId(int user_id, int test_id) {
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
    public byte[] getStudentAnswerFileById(int user_id, int student_answer_file_id) {
        byte[] file_contents = null;
        try {
            getStudentAnswerFileByIdStatement.setInt(1, user_id);
            getStudentAnswerFileByIdStatement.setInt(2, student_answer_file_id);
            ResultSet resultSet = getStudentAnswerFileByIdStatement.executeQuery();
            if (resultSet.first()) {
                file_contents = resultSet.getBytes(1);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return file_contents;
    }
}
