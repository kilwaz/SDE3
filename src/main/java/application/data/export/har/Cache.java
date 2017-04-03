package application.data.export.har;

public class Cache extends HarObject {
    private HarObject beforeRequest = null; // Optional
    private HarObject afterRequest = null; // Optional

    public Cache() {
        super("cache", HarObject.OBJECT);
    }

    public void setBeforeRequest(String beforeRequest) {
        this.beforeRequest = new HarObject("beforeRequest", beforeRequest);
    }

    public void setAfterRequest(String afterRequest) {
        this.afterRequest = new HarObject("afterRequest", afterRequest);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(beforeRequest);
        output.writeObject(afterRequest);
        output.writeObject(getComment());
        output.endObject();
    }
}
