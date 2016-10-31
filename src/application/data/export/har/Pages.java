package application.data.export.har;

import java.util.ArrayList;
import java.util.List;

public class Pages extends HarObject {
    private List<Page> pages = new ArrayList<>();

    public Pages() {
        super("pages", HarObject.LIST);
    }

    public List<Page> getPages() {
        return pages;
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        pages.forEach(output::writeObject);
        output.endObject();
    }
}
