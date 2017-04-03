package application.data.export.har;

import java.util.ArrayList;
import java.util.List;

public class QueryString extends HarObject {
    private List<QueryStringEntry> queryString = new ArrayList<>();

    public QueryString() {
        super("queryString", HarObject.LIST);
    }

    public void addQueryString(QueryStringEntry queryStringEntry) {
        queryString.add(queryStringEntry);
    }

    public List<QueryStringEntry> getQueryString() {
        return queryString;
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        queryString.forEach(output::writeObject);
        output.endObject();
    }
}
