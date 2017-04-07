package sde.application.data.export.har;

public class HarObject {
    public static final int STRING = 0;
    public static final int LIST = 1;
    public static final int OBJECT = 2;
    public static final int LIST_OBJECT = 3;
    public static final int NUMBER = 4;

    private HarObject comment = null;
    private String objectName = "";
    private Object objectValue = "";
    private int type;

    public HarObject(String objectName, int type) {
        this.objectName = objectName;
        this.type = type;
    }

    public HarObject(String objectName, String objectValue) {
        this.objectName = objectName;
        this.objectValue = objectValue;
        this.type = STRING;
    }

    public HarObject(String objectName, Integer objectValue) {
        this.objectName = objectName;
        this.objectValue = objectValue;
        this.type = NUMBER;
    }

    public void export() {
        HarOutputStreamWriter output = new HarOutputStreamWriter("C:\\Users\\alex\\Downloads\\output.har");
        output.write("{");
        output.startObject();
        output.writeObject(this);
        output.endObject();
        output.write("}");
        output.close();
    }

    public void write(HarOutputStreamWriter output) {
        // To be overridden
    }

    public String getObjectName() {
        return objectName;
    }

    public Object getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(String objectValue) {
        this.objectValue = objectValue;
    }

    public HarObject getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = new HarObject("comment", comment);
    }

    public int getType() {
        return type;
    }
}
