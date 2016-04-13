package application.test.core;

public class TestInputList {
    private String name;
    private String[] list;

    public TestInputList(String name, String[] list) {
        this.name = name;
        this.list = list;
    }

    public TestInputList(String name, String val) {
        this.name = name;
        this.list = new String[]{val};
    }

    public String getName() {
        return name;
    }

    public String[] getList() {
        return list;
    }
}
