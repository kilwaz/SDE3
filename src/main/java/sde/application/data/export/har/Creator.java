package sde.application.data.export.har;

import sde.application.utils.AppParams;

public class Creator extends HarObject {
    private HarObject name = new HarObject("name", AppParams.APP_TITLE);
    private HarObject version = new HarObject("version", AppParams.getAppVersionString());

    public Creator() {
        super("creator", OBJECT);
    }

    public HarObject getName() {
        return name;
    }

    public HarObject getVersion() {
        return version;
    }

    public void write(HarOutputStreamWriter output) {
        output.startObject();
        output.writeObject(name);
        output.writeObject(version);
        output.writeObject(getComment());
        output.endObject();
    }
}
