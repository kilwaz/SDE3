package application.data.export.har;

import application.error.Error;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;

public class HarOutputStreamWriter {
    private OutputStreamWriter output = null;
    private Integer indentation = 0;
    private HashMap<Integer, Boolean> firstObjectMap = new HashMap<>();  // Used to keep track of when we need to add a comma at the end of the line of the import for lists

    public HarOutputStreamWriter(String fileLocation) {
        try {
            output = new OutputStreamWriter(
                    new FileOutputStream(fileLocation),
                    Charset.forName("UTF-8").newEncoder()
            );
        } catch (FileNotFoundException ex) {
            Error.SDE_FILE_NOT_FOUND.record().create(ex);
        }
    }

    public void writeNewLine() {
        write(System.lineSeparator() + StringUtils.repeat("\t", indentation));
    }

    public void startObject() {
        indentation++;
        firstObjectMap.put(indentation, true);  // Marked as the first object in the list
        writeNewLine();
    }

    public void endObject() {
        indentation--;
        writeNewLine();
    }

    public void writeObject(HarObject harObject) {
        if (harObject != null) {
            if (firstObjectMap.get(indentation) == null || firstObjectMap.get(indentation)) {
                firstObjectMap.put(indentation, false);  // Future objects in the list have a comma on the previous entry
            } else {
                write(",");
                writeNewLine();
            }
            if (harObject.getType() == HarObject.STRING) {
                write("\"" + harObject.getObjectName() + "\" : \"" + StringEscapeUtils.escapeJson(harObject.getObjectValue().toString()) + "\"");
            } else if (harObject.getType() == HarObject.NUMBER) {
                write("\"" + harObject.getObjectName() + "\" : " + StringEscapeUtils.escapeJson(harObject.getObjectValue().toString()));
            } else if (harObject.getType() == HarObject.OBJECT) {
                write("\"" + harObject.getObjectName() + "\": {");
                harObject.write(this);
                write("}");
            } else if (harObject.getType() == HarObject.LIST) {
                write("\"" + harObject.getObjectName() + "\": [");
                harObject.write(this);
                write("]");
            } else if (harObject.getType() == HarObject.LIST_OBJECT) {
                write("{");
                harObject.write(this);
                write("}");
            }
        }
    }

    public void write(String str) {
        try {
            output.write(str);
        } catch (IOException ex) {
            Error.HAR_OUTPUT_WRITE_FAILED.record().create(ex);
        }
    }

    public void close() {
        try {
            output.close();
        } catch (IOException ex) {
            Error.HAR_OUTPUT_CLOSE_FAILED.record().create(ex);
        }
    }
}
