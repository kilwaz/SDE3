package sde.application.data.export.har;

public class Content extends HarObject {
    private HarObject size = new HarObject("size", new Integer(-1)); // Defaults to -1
    private HarObject compression = new HarObject("compression", ""); // Defaults to blank
    private HarObject mimeType = new HarObject("mimeType", ""); // Defaults to blank
    private HarObject text = new HarObject("text", ""); // Defaults to blank
    private HarObject encoding = new HarObject("encoding", ""); // Defaults to blank

    public Content() {
        super("content", HarObject.OBJECT);
    }

    public void setSize(Integer size) {
        this.size = new HarObject("size", size);
    }

    public void setCompression(String compression) {
        this.compression = new HarObject("compression", compression);
    }

    public void setMimeType(String mimeType) {
        this.mimeType = new HarObject("mimeType", mimeType);
    }

    public void setText(String text) {
        this.text = new HarObject("text", text);
    }

    public void setEncoding(String encoding) {
        this.encoding = new HarObject("encoding", encoding);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(size);
        output.writeObject(compression);
        output.writeObject(mimeType);
        output.writeObject(text);
        output.writeObject(encoding);
        output.writeObject(getComment());
        output.endObject();
    }
}
