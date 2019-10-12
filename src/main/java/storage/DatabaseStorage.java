package storage;

import security.HashedPassword;
import security.PasswordSecurity;
import javax.sql.rowset.serial.SerialBlob;
import java.util.*;
import java.sql.*;

public class DatabaseStorage extends PersistentStorage {
    private Connection connection;
    private PreparedStatement createUserStatement;
    private PreparedStatement validateUserStatement;
    private PreparedStatement checkUserWithUsernameExistsStatement;
    private PreparedStatement getTestsByUserStatement;
    private PreparedStatement createTestFileStatement;
    private PreparedStatement createTestStatement;
    private PreparedStatement getTestByIdStatement;

    private final String createUserSQL = "INSERT INTO users (user_id, username, password_hash, password_salt, email, permissions) VALUES (default, ?, ?, ?, ?, ?)";

    private final String validateUserSQL = "SELECT * FROM users WHERE username = ?";
    private final int USERS_TABLE_USER_ID_COLUMN = 1;
    private final int USERS_TABLE_USERNAME_COLUMN = 2;
    private final int USERS_TABLE_PASSWORD_HASH_COLUMN = 3;
    private final int USERS_TABLE_PASSWORD_SALT_COLUMN = 4;
    private final int USERS_TABLE_EMAIL_COLUMN = 5;
    private final int USERS_TABLE_PERMISSIONS_COLUMN = 6;

    private final String checkUserWithUsernameExistsSQL = "SELECT username FROM users WHERE username = ?";

    private final String createTestFileSQL = "INSERT INTO test_files (test_file_id, test_file, test_file_name, test_file_type) VALUES (default, ?, ?, ?)";

    private final String createTestSQL = "INSERT INTO tests (test_id, user_id, test_name, test_description, blank_test_file, answers_test_file) VALUES (default, ?, ?, ?, ?, ?)";

    private final String getTestsByUserSQL = "SELECT * FROM tests WHERE user_id = ?";
    private final int TESTS_TABLE_TEST_ID_COLUMN = 1;
    private final int TESTS_TABLE_USER_ID_COLUMN = 2;
    private final int TESTS_TABLE_TEST_NAME_COLUMN = 3;
    private final int TESTS_TABLE_TEST_DESCRIPTION_COLUMN = 4;
    private final int TESTS_TABLE_BLANK_TEST_FILE_COLUMN = 5;
    private final int TESTS_TABLE_ANSWERS_TEST_FILE_COLUMN = 6;

    private final String getTestByIdSQL = "SELECT * FROM tests WHERE user_id = ? AND test_id = ?";

    @Override
    protected void initializeStorageMethod() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/markspace", "root", "GradingIsFunForYouXDXD42069!!!");
            createUserStatement = connection.prepareStatement(createUserSQL);
            validateUserStatement = connection.prepareStatement(validateUserSQL);
            checkUserWithUsernameExistsStatement = connection.prepareStatement(checkUserWithUsernameExistsSQL);
            createTestFileStatement = connection.prepareStatement(createTestFileSQL, Statement.RETURN_GENERATED_KEYS);
            createTestStatement = connection.prepareStatement(createTestSQL, Statement.RETURN_GENERATED_KEYS);
            getTestsByUserStatement = connection.prepareStatement(getTestsByUserSQL);
            getTestByIdStatement = connection.prepareStatement(getTestByIdSQL);
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

    private int createTestFile(byte[] test_file, String name, String file_type) {
        try {
            SerialBlob test_file_blob = new SerialBlob(test_file);
            createTestFileStatement.setBlob(1, test_file_blob);
            createTestFileStatement.setString(2, name);
            createTestFileStatement.setString(3, file_type);
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
            createTestStatement.setInt(1, user_id);
            createTestStatement.setString(2, name);
            createTestStatement.setString(3, description);
            createTestStatement.setInt(4, createTestFile(blank_test_file, blank_test_file_name, blank_test_file_type));
            createTestStatement.setInt(5, createTestFile(answers_test_file, answers_test_file_name, answers_test_file_type));
            createTestStatement.executeUpdate();
            ResultSet generated_keys = createTestStatement.getGeneratedKeys();
            generated_keys.first();
            test_id = generated_keys.getInt(1);
        }
        catch (SQLException e) {
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
}
