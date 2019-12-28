package storage;

public class TestFile {
    private int id;
    private byte[] data;
    private String name;
    private String type;
    private int number_of_pages;

    public TestFile(int id, byte[] data, String name, String type, int number_of_pages) {
        this.id = id;
        this.data = data;
        this.name = name;
        this.type = type;
        this.number_of_pages = number_of_pages;
    }

    public int getId() {
        return id;
    }

    public byte[] getData() {
        return data;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getNumberOfPages() {
        return number_of_pages;
    }
}
