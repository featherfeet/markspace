package storage;

/**
 * Abstract class representing the "persistent layer" of the application. So far, the only implementation of this is storage.DatabaseStorage, which uses a database.
 * @see storage.DatabaseStorage
 */

import java.util.List;
import java.util.Map;

/**
 * Abstract class that represents the "persistent layer" of the application, storing everything (tests, users, answers, etc.) that must be permanently saved.
 * Only one of these per application should ever be created; it should be shared between all of the controllers.
 */
public abstract class PersistentStorage {
    /**
     * Create a new PersistentStorage, initializing whatever storage method backs the application. Only one per running application should be created; it should be shared between all controllers.
     */
    public PersistentStorage() {
        initializeStorageMethod();
    }

    /**
     * Initialize whatever storage method is being used as the persistent layer. Usually a database connection.
     */
    protected abstract void initializeStorageMethod();

    /**
     * Create a new user. Note that this does not check if an user with the same username already exists; use PersistentStorage.checkUserWithUsernameExists(username) to avoid duplicating/overwriting existing users.
     * @param username The username of the user to create.
     * @param password The plaintext password of the user to create.
     * @param email The email of the user to create.
     * @param permissions An array of permissions that the user should have. Currently stored but not actually used (all users have all permissions right now).
     */
    public abstract void createUser(String username, String password, String email, UserPermission[] permissions);

    /**
     * Check a user's credentials (used for logging in existing users).
     * @param username The username that the user inputted.
     * @param password The plaintext password that the user inputted.
     * @return true if valid credentials, false if not.
     */
    public abstract int validateUser(String username, String password);

    /**
     * Check whether a user with a given username already exists (used for the creation of new users).
     * @param username The username to check.
     * @return true if the user already exists, false if not.
     */
    public abstract boolean checkUserWithUsernameExists(String username);

    /**
     * Create a new test for a given user.
     * @param user_id The user id of the user creating the test (they will become the 'owner' of the test).
     * @param name The human-readable title (name) of the test.
     * @param description The human-readable plaintext textual description of the test.
     * @param blank_test_file The raw PDF data of the test, without answers written in.
     * @param blank_test_file_name The file name of the PDF of the test without answers.
     * @param blank_test_file_type The type (a string like 'pdf' or 'docx') of the blank test file. Will always be 'pdf' for now.
     * @param answers_test_file The raw PDF data of the test, with answers written in.
     * @param answers_test_file_name The file name of the PDF of the test with answers.
     * @param answers_test_file_type The type (a string like 'pdf' or 'docx') of the test file with answers. Will always be 'pdf' for now.
     * @return The id of the test that was created.
     */
    public abstract int createTest(int user_id, String name, String description, byte[] blank_test_file, String blank_test_file_name, String blank_test_file_type, byte[] answers_test_file, String answers_test_file_name, String answers_test_file_type);

    /**
     * Retrieve all tests created/owned by a specific user.
     * @param user_id The id of the user for whom you want to retrieve tests.
     * @return An array of all tests created/owned by the user with id user_id.
     */
    public abstract Test[] getTestsByUser(int user_id);

    /**
     * Retrieve a test by user id and test_id.
     * @param user_id The user id of the user who owns (created) the test. Providing this prevents users from maliciously or accidentally accessing each other's tests.
     * @param test_id The test id of the test you are trying to retrieve.
     * @return A Test object of the requested test. Note that changing this returned Test object has no effect on the actual persistently stored data. Returns null if the test couldn't be found or was owned by another user.
     */
    public abstract Test getTestById(int user_id, int test_id);

    /**
     * Retrieve a test file (a previously-uploaded PDF containing test questions and possibly example answers as well) by its id.
     * @param user_id The id of the user who uploaded the file.
     * @param test_file_id The id of the test file.
     * @return A TestFile object of the requested test file. Note that changing this returned TestFile object has no effect on the actual persistently stored data. Returns null if not found or owned by another user.
     */
    public abstract TestFile getTestFileById(int user_id, int test_file_id);

    /**
     * Get the number of pages in a test file (a previously-uploaded PDF containing test questions and possibly example answers as well) by its id.
     * @param user_id The user id of the user who uploaded the file.
     * @param test_file_id The id of the test file.
     * @return The number of pages in the test file as an integer. Returns -1 if not found or owned by another user.
     */
    public abstract int getNumberOfPagesInTestFileById(int user_id, int test_file_id);

    /**
     * Creates new test questions.
     * @param test_file_id All test questions must be attached to a specific test file. This specifies the id of the test file. Normally, test questions will be associated with a test file that has example solutions written in.
     * @param user_id The user id of the user who created the questions.
     * @param questions An array of storage.TestQuestion objects representing the test questions to be added to the persistent storage.
     */
    public abstract void createQuestions(int test_file_id, int user_id, TestQuestion[] questions);

    /**
     * Retrieve all questions associated with a specific test file.
     * @param user_id The user id of the user who uploaded the test file.
     * @param test_file_id The id of the test file that the questions are attached to.
     * @return An array of all test questions by user user_id attached to the test file test_file_id. Returns an empty array if no questions are found.
     */
    public abstract TestQuestion[] getQuestionsByTestFileId(int user_id, int test_file_id);

    /**
     * Retrieve all questions associated with a specific test. This will call getQuestionsByTestFileId for the test file of the specified test that has example solutions written in (i. e. it does not query for questions attached to the blank test file).
     * @param user_id The id of the user who created the questions being queried.
     * @param test_id The id of the test that the questions are attached to.
     * @return An array of all test questions matching the criteria. Returns an empty array if none are found.
     */
    public abstract TestQuestion[] getQuestionsByTestId(int user_id, int test_id);

    /**
     * Delete a test and all associated test files and questions.
     * @param user_id The user id of the user who owns the test.
     * @param test_id The id of the test to delete.
     */
    public abstract void deleteTestById(int user_id, int test_id);

    /**
     * Create a student answer file (a scanned PDF file containing students' responses to test questions).
     * @param user_id The user id of the user uploading the PDF.
     * @param test_id The test id of the test that the answers correspond to.
     * @param student_answer_file The raw, binary PDF data of the uploaded file.
     * @param student_answer_file_name The name of the uploaded file.
     * @param student_answer_file_type The type of the uploaded file. For now, this is always "pdf".
     * @param number_of_pages The number of pages in the uploaded file.
     * @return The id of the student answer file that was created.
     */
    public abstract int createStudentAnswerFile(int user_id, int test_id, byte[] student_answer_file, String student_answer_file_name, String student_answer_file_type, int number_of_pages);

    /**
     * Retrieve all student answer files associated with a particular test.
     * @param user_id The user id of the user who owns the test.
     * @param test_id The test id of the test that the student answers correspond to.
     * @return A Map&lt;Integer, String&gt; where the keys are student answer files' ids and the values are the student answer files' names.
     */
    public abstract Map<Integer, String> getStudentAnswerFilesByTestId(int user_id, int test_id);

    /**
     * Retrieve the contents of a student answer file by its id.
     * @param user_id The user id of the user who uploaded the file.
     * @param student_answer_file_id The id of the student answer file being requested.
     * @return The raw, binary PDF data of the student answer file.
     */
    public abstract byte[] getStudentAnswerFileById(int user_id, int student_answer_file_id);

    /**
     * Retrieve a map that maps student answer file ids to the number of pages in those student answer files.
     * @param user_id The user id of the user who uploaded the answers.
     * @param test_id The id of the test that the student answer files are attached to.
     * @return A map where keys are student answer file ids and values are the number of pages in those student answer files.
     */
    public abstract Map<Integer, Integer> getStudentAnswerFilesNumberOfPages(int user_id, int test_id);

    /**
     * Create new student answers (can be blank for later grading).
     * @param user_id The user id of the user creating the student answers.
     * @param student_answers An array of student answer objects to add. They can be "blank" student answer objects that haven't yet been graded. createStudentAnswers will assign them id's.
     */
    public abstract void createStudentAnswers(int user_id, StudentAnswer[] student_answers);

    /**
     * Retrieve a single test question by its id.
     * @param user_id The user id of the user who owns the test question.
     * @param question_id The id of the question to fetch.
     * @return A TestQuestion object with the specified id.
     */
    public abstract TestQuestion getQuestionById(int user_id, int question_id);

    /**
     * Retrieve all student answers attached to a specific student answer file.
     * @param user_id The user id of the user who created the student answers.
     * @param student_answer_file_id The id of the student answer file that the desired student answers are attached to.
     * @return An ArrayList of student answer objects that were attached to the specified student answer file.
     */
    public abstract List<StudentAnswer> getStudentAnswersByStudentAnswerFileId(int user_id, int student_answer_file_id);

    /**
     * Set the score (number of points earned by the student) for a student answer. Can be negative. Can be more than points_possible (i. e. extra credit). Can be zero. This is also used for "identification" questions where the student hand-writes their name on the page.
     * @param user_id The user id of the user who uploaded the student answer being scored.
     * @param student_answer_id The id of the student answer being scored.
     * @param score The score to give. Or, if this is an "identification" question, the transcribed name/ID number of the student.
     */
    public abstract void scoreStudentAnswer(int user_id, int student_answer_id, String score);
}