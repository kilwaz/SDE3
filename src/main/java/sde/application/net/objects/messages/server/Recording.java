package sde.application.net.objects.messages.server;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import sde.application.net.objects.NetworkObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Recording extends NetworkObject {

    private static Logger log = Logger.getLogger(Recording.class);

    private String recordingFileName = "";
    private Byte[] recording = null;
    private Long fileLength = -1l;
    private Boolean fileExists = false;

    public Recording() {
        super();
    }

    public Recording setRecordingFileName(String recordingFileName) {
        this.recordingFileName = recordingFileName;

        File recordingFile = new File(recordingFileName);
        fileExists = recordingFile.exists();
        fileLength = recordingFile.length();

        log.info("Requested file name is: " + recordingFileName);
        log.info("Setting length as " + recordingFile.length() + " exists? " + fileExists);

        return this;
    }

    public String getRecordingFileName() {
        return recordingFileName;
    }

    public Long getFileLength() {
        return fileLength;
    }

    public Boolean getFileExists() {
        return fileExists;
    }

    public Recording loadFile() {
        File recordingFile = new File(recordingFileName);
        if (recordingFile.exists()) {
            FileInputStream fis = null;
            try {
                fis = FileUtils.openInputStream(recordingFile);
                byte[] primitiveRecording = IOUtils.toByteArray(new FileInputStream(recordingFile));
                recording = new Byte[primitiveRecording.length];

                recording = ArrayUtils.toObject(primitiveRecording);



                log.info("Recording has length " + primitiveRecording.length);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return this;
    }

    public void saveFile() {
        log.info("Save file method");
        log.info("Recording reference: " + recordingFileName);
        log.info("Recording? " + (recording == null));
        log.info("Recording length is " + recording.length);

        log.info("File length is " + fileLength);
        log.info("File exists is " + fileExists);

        try {
            FileUtils.writeByteArrayToFile(new File("C:\\Users\\alex\\Downloads\\" + recordingFileName), ArrayUtils.toPrimitive(recording));
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("File save complete");
    }
}
