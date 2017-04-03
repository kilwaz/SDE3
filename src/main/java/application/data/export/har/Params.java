package application.data.export.har;

import java.util.ArrayList;
import java.util.List;

public class Params extends HarObject {
    private List<Param> params = new ArrayList<>();

    public Params() {
        super("params", HarObject.LIST);
    }

    public void addParam(Param param) {
        params.add(param);
    }

    public List<Param> getParams() {
        return params;
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        params.forEach(output::writeObject);
        output.endObject();
    }
}
