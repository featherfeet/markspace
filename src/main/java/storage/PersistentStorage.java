package storage;

public abstract class PersistentStorage {
    public PersistentStorage() {
        initializeStorageMethod();
    }
    protected abstract void initializeStorageMethod();
    public abstract void createUser(String username, String password, String email, UserPermission[] permissions);
    public abstract int validateUser(String username, String password);
    public abstract boolean checkUserWithUsernameExists(String username);
    public abstract int createTest(int user_id, String name, String description, byte[] blank_test_file, String blank_test_file_name, String blank_test_file_type, byte[] answers_test_file, String answers_test_file_name, String answers_test_file_type);
    public abstract Test[] getTestsByUser(int user_id);
    public abstract Test getTestById(int user_id, int test_id);
    public abstract TestFile getTestFileById(int user_id, int test_file_id);
    public abstract int getNumberOfPagesInTestFileById(int user_id, int test_file_id);
    public abstract void createQuestions(int test_file_id, int user_id, TestQuestion[] questions);
    public abstract TestQuestion[] getQuestionsByTestFileId(int user_id, int test_file_id);
    public abstract TestQuestion[] getQuestionsByTestId(int user_id, int test_id);
    public abstract void deleteTestById(int user_id, int test_id);
}