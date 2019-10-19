package storage;

public class TestFile {
    private int id;
    private byte[] data;
    private String name;
    private String type;

    public TestFile(int id, byte[] data, String name, String type) {
        this.id = id;
        this.data = data;
        this.name = name;
        this.type = type;
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
}
