package sde.application.data.export.har;

public class BeforeRequest extends HarObject {
    private HarObject expires = new HarObject("expires", ""); // Defaults to blank
    private HarObject lastAccess = new HarObject("lastAccess", ""); // Defaults to blank
    private HarObject eTag = new HarObject("eTag", ""); // Defaults to blank
    private HarObject hitCount = new HarObject("hitCount", new Integer(-1)); // Defaults to -1

    public BeforeRequest() {
        super("beforeRequest", OBJECT);
    }

    public void setExpires(String expires) {
        this.expires = new HarObject("expires", expires);
    }

    public void setLastAccess(String lastAccess) {
        this.lastAccess = new HarObject("lastAccess", lastAccess);
    }

    public void setETag(String eTag) {
        this.eTag = new HarObject("eTag", eTag);
    }

    public void setHitCount(Integer hitCount) {
        this.hitCount = new HarObject("hitCount", hitCount);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(expires);
        output.writeObject(lastAccess);
        output.writeObject(eTag);
        output.writeObject(hitCount);
        output.writeObject(getComment());
        output.endObject();
    }
}
