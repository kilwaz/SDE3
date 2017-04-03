package sde.application.data.export.har;

public class PageTimings extends HarObject {
    private HarObject onContentLoad = new HarObject("onContentLoad", new Integer(-1)); // Default value of -1
    private HarObject onLoad = new HarObject("onLoad", new Integer(-1)); // Default value of -1

    public PageTimings() {
        super("pageTimings", HarObject.OBJECT);
    }

    public void setOnContentLoad(Integer onContentLoad) {
        this.onContentLoad = new HarObject("onContentLoad", onContentLoad);
    }

    public void setOnLoad(Integer onLoad) {
        this.onLoad = new HarObject("onLoad", onLoad);
    }

    public void write(HarOutputStreamWriter output) {
        output.startObject();
        output.writeObject(onContentLoad);
        output.writeObject(onLoad);
        output.writeObject(getComment());
        output.endObject();
    }
}
