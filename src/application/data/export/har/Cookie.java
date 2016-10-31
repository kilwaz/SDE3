package application.data.export.har;

public class Cookie extends HarObject {
    private HarObject name = new HarObject("name", ""); // Defaults to blank
    private HarObject value = new HarObject("value", ""); // Defaults to blank
    private HarObject path = null; // Optional
    private HarObject domain = null; // Optional
    private HarObject expires = null; // Optional
    private HarObject httpOnly = null; // Optional
    private HarObject secure = null; // Optional

    public Cookie() {
        super("cookies", HarObject.LIST_OBJECT);
    }

    public void setName(String name) {
        this.name = new HarObject("name", name);
    }

    public void setValue(String value) {
        this.value = new HarObject("value", value);
    }

    public void setPath(String path) {
        this.path = new HarObject("path", path);
    }

    public void setDomain(String domain) {
        this.domain = new HarObject("domain", domain);
    }

    public void setExpires(String expires) {
        this.expires = new HarObject("expires", expires);
    }

    public void setHttpOnly(String httpOnly) {
        this.httpOnly = new HarObject("httpOnly", httpOnly);
    }

    public void setSecure(String secure) {
        this.secure = new HarObject("secure", secure);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(name);
        output.writeObject(value);
        output.writeObject(path);
        output.writeObject(domain);
        output.writeObject(expires);
        output.writeObject(httpOnly);
        output.writeObject(secure);
        output.writeObject(getComment());
        output.endObject();
    }
}
