package sde.application.data.export.har;

public class Param extends HarObject {
    private HarObject name = new HarObject("name", ""); // Defaults to blank
    private HarObject value = null; // Optional
    private HarObject fileName = null; // Optional
    private HarObject contentType = null; // Optional

    public Param() {
        super("param", HarObject.LIST_OBJECT);
    }

    public void setName(String name) {
        this.name = new HarObject("name", name);
    }

    public void setValue(String value) {
        this.value = new HarObject("value", value);
    }

    public void setFileName(String fileName) {
        this.fileName = new HarObject("fileName", fileName);
    }

    public void setContentType(String contentType) {
        this.contentType = new HarObject("contentType", contentType);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(name);
        output.writeObject(value);
        output.writeObject(fileName);
        output.writeObject(contentType);
        output.writeObject(getComment());
        output.endObject();
    }
}
