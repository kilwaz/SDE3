package application.data.export.har;

public class Browser extends HarObject {
    private HarObject name = new HarObject("name", "");
    private HarObject version = new HarObject("version", "");

    public Browser() {
        super("browser", HarObject.OBJECT);
    }

    public HarObject getName() {
        return name;
    }

    public void setName(String name) {
        this.name = new HarObject("name", name);
    }

    public HarObject getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = new HarObject("version", version);
    }

    public void write(HarOutputStreamWriter output) {
        output.startObject();
        output.writeObject(name);
        output.writeObject(version);
        output.writeObject(getComment());
        output.endObject();
    }
}
