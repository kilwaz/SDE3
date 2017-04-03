package application.data.export.har;

public class Entry extends HarObject {
    private HarObject pageRef = null;
    private HarObject startedDateTime = null;
    private HarObject time = null;

    private Request request = new Request();
    private Response response = new Response();
    private Cache cache = new Cache();
    private Timings timings = new Timings();

    private HarObject serverIPAddress = null;
    private HarObject connection = null;

    public Entry() {
        super("entry", HarObject.LIST_OBJECT);
    }

    public void setStartedDateTime(String startedDateTime) {
        this.startedDateTime = new HarObject("startedDateTime", startedDateTime);
    }

    public void setTime(Integer time) {
        this.time = new HarObject("time", time);
    }

    public void setServerIPAddress(String serverIPAddress) {
        this.serverIPAddress = new HarObject("serverIPAddress", serverIPAddress);
    }

    public void setConnection(String connection) {
        this.connection = new HarObject("connection", connection);
    }

    public HarObject getPageRef() {
        return pageRef;
    }

    public void setPageRef(HarObject pageRef) {
        this.pageRef = pageRef;
    }

    public Request getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }

    public Cache getCache() {
        return cache;
    }

    public Timings getTimings() {
        return timings;
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(pageRef);
        output.writeObject(startedDateTime);
        output.writeObject(time);
        output.writeObject(request);
        output.writeObject(response);
        output.writeObject(cache);
        output.writeObject(timings);
        output.writeObject(serverIPAddress);
        output.writeObject(connection);
        output.writeObject(getComment());
        output.endObject();
    }
}
