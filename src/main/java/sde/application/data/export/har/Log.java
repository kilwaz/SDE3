package sde.application.data.export.har;

public class Log extends HarObject {
    private HarObject version = new HarObject("version", "1.2");

    private Creator creator = new Creator();
    private Browser browser = new Browser();
    private Pages pages = new Pages();
    private Entries entries = new Entries();

    public Log() {
        super("log", HarObject.OBJECT);
    }

    public HarObject getVersion() {
        return version;
    }

    public Creator getCreator() {
        return creator;
    }

    public Browser getBrowser() {
        return browser;
    }

    public Pages getPages() {
        return pages;
    }

    public Entries getEntries() {
        return entries;
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(version);
        output.writeObject(creator);
        output.writeObject(browser);
        output.writeObject(pages);
        output.writeObject(entries);
        output.writeObject(getComment());
        output.endObject();
    }
}
