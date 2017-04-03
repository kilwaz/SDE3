package application.data.export.har;

public class PostData extends HarObject {
    private HarObject mimeType = new HarObject("mimeType", ""); // Defaults to blank
    private Params params = new Params();
    private HarObject text = new HarObject("text", ""); // Defaults to blank

    public PostData() {
        super("postData", HarObject.OBJECT);
    }

    public Params getParams() {
        return params;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = new HarObject("mimeType", mimeType);
    }

    public void setText(String text) {
        this.text = new HarObject("text", text);
    }

    public void write(HarOutputStreamWriter output) {
        // Order of methods is the same as order written
        output.startObject();
        output.writeObject(mimeType);
        output.writeObject(params);
        output.writeObject(text);
        output.writeObject(getComment());
        output.endObject();
    }
}
