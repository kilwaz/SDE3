package application.data.export.har;

public class Page extends HarObject {
    private HarObject startedDateTime = new HarObject("startedDateTime", ""); // Defaults to blank
    private HarObject id = new HarObject("id", ""); // Defaults to blank
    private HarObject title = new HarObject("title", ""); // Defaults to blank
    private PageTimings pageTimings = new PageTimings();

    public Page() {
        super("page", HarObject.LIST_OBJECT);
    }

    public void setStartedDateTime(String startedDateTime) {
        this.startedDateTime = new HarObject("startedDateTime", startedDateTime);
    }

    public void setId(String id) {
        this.id = new HarObject("id", id);
    }

    public void setTitle(String title) {
        this.title = new HarObject("title", title);
    }

    public PageTimings getPageTimings() {
        return pageTimings;
    }

    public void write(HarOutputStreamWriter output) {
        output.startObject();
        output.writeObject(startedDateTime);
        output.writeObject(id);
        output.writeObject(title);
        output.writeObject(pageTimings);
        output.writeObject(getComment());
        output.endObject();
    }
}
