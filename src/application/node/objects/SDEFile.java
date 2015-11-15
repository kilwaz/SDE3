package application.node.objects;

import application.data.model.DatabaseObject;
import application.error.Error;
import application.node.implementations.FileStoreNode;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.UUID;

public class SDEFile extends DatabaseObject {
    private FileStoreNode parentNode = null;
    private File file = null;

    private static Logger log = Logger.getLogger(SDEFile.class);

    public SDEFile(UUID uuid, FileStoreNode parentNode) {
        super(uuid);
        this.parentNode = parentNode;
        String userHome = System.getProperty("user.home");

        File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
        this.file = new File(root, "sdeFiles/" + uuid + ".tmp");
        if (!file.exists()) {
            try {
                Boolean createResult = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public SDEFile(UUID uuid, File file, FileStoreNode parentNode) {
        super(uuid);
        this.parentNode = parentNode;
        this.file = file;
    }

    public String getParentUuid() {
        if (parentNode != null) {
            return parentNode.getUuidString();
        }
        return null;
    }

    public InputStream getInputStream() {
        try {
            if (file != null) {
                return new FileInputStream(file);
            }
        } catch (FileNotFoundException ex) {
            Error.SDE_FILE_NOT_FOUND.record().create(ex);
        }

        return null;
    }

    public File getFile() {
        return file;
    }

    public void setFile(String location) {
        this.file = new File(location);
        if (!file.exists()) {
            try {
                Boolean createResult = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        save();
    }

    public void setFile(File file) {
        this.file = file;
        if (!file.exists()) {
            try {
                Boolean createResult = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        save();
    }

    public FileStoreNode getParentNode() {
        return parentNode;
    }
}
