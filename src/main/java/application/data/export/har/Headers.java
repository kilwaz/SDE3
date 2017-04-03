package application.data.export.har;

import java.util.ArrayList;
import java.util.List;

public class Headers extends HarObject {
    private List<Header> headers = new ArrayList<>();

    public Headers() {
        super("headers", HarObject.LIST);
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        headers.forEach(output::writeObject);
        output.endObject();
    }
}
