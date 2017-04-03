package sde.application.data.export.har;

public class Header extends HarObject {
    private HarObject name = new HarObject("name", "");
    private HarObject value = new HarObject("value", "");

    public Header() {
        super("header", LIST_OBJECT);
    }

    public void setName(String name) {
        this.name = new HarObject("name", name);
    }

    public void setValue(String value) {
        this.value = new HarObject("value", value);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(name);
        output.writeObject(value);
        output.writeObject(getComment());
        output.endObject();
    }
}
