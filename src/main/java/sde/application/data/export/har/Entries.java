package sde.application.data.export.har;

import java.util.ArrayList;
import java.util.List;

public class Entries extends HarObject {
    private List<Entry> entries = new ArrayList<>();

    public Entries() {
        super("entries", HarObject.LIST);
    }

    public void addEntry(Entry entry) {
        entries.add(entry);
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        entries.forEach(output::writeObject);
        output.endObject();
    }
}