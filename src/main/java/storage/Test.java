package storage;

public class Test {
    private int id;
    private String name;
    private String description;
    private int blank_test_file_id;
    private int answers_test_file_id;

    public Test(int id, String name, String description, int blank_test_file_id, int answers_test_file_id) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.blank_test_file_id = blank_test_file_id;
        this.answers_test_file_id = answers_test_file_id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getBlankTestFile() {
        return blank_test_file_id;
    }

    public int getAnswersTestFile() {
        return answers_test_file_id;
    }
}
