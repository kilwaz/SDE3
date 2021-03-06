package sde.application.net.objects.messages.server;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import sde.application.error.Error;
import sde.application.net.objects.NetworkObject;
import sde.application.net.objects.messages.client.DeleteRecording;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Recording extends NetworkObject {

    private static Logger log = Logger.getLogger(Recording.class);

    private String recordingFileName = "";
    private String localFileLocation = "";
    private Byte[] recording = null;
    private Long fileLength = -1l;
    private Boolean fileExists = false;

    public Recording() {
        super();
    }

    public Recording setLocalFileLocation(String localFileLocation) {
        this.localFileLocation = localFileLocation;
        return this;
    }

    public String getLocalFileLocation() {
        return localFileLocation;
    }

    public Recording setRecordingFileName(String recordingFileName) {
        this.recordingFileName = recordingFileName;

        File recordingFile = new File(recordingFileName);
        fileExists = recordingFile.exists();
        fileLength = recordingFile.length();

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
        try {
            log.info("Saving recording to " + localFileLocation);

            FileUtils.writeByteArrayToFile(new File(localFileLocation), ArrayUtils.toPrimitive(recording));
        } catch (IOException ex) {
            Error.ERROR_SAVING_TEST_RECORDING.record().create(ex);
        }
    }

    public List<NetworkObject> getResponses() {
        List<NetworkObject> networkObjects = new ArrayList<>();

        // Clean up the file on the server once we have received and finished this
        networkObjects.add(new DeleteRecording().setFileName(recordingFileName));

        return networkObjects;
    }
}
