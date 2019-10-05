package storage;

public abstract class PersistentStorage {
    public PersistentStorage() {
        initializeStorageMethod();
    }
    protected abstract void initializeStorageMethod();
    public abstract void createUser(String username, String password, String email, UserPermission[] permissions);
    public abstract int validateUser(String username, String password);
    public abstract void createTest(int user_id, String name, String description, byte[] blank_test_file, String blank_test_file_name, String blank_test_file_type, byte[] answers_test_file, String answers_test_file_name, String answers_test_file_type);
    public abstract Test[] getTestsByUser(int user_id);
}