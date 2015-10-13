package application.node.objects;

import application.data.DataBank;
import application.node.implementations.FileStoreNode;
import org.apache.log4j.Logger;

import java.io.*;

public class SDEFile {
    private Integer id = -1;
    private FileStoreNode parentNode = null;
    private File file = null;

    private static Logger log = Logger.getLogger(SDEFile.class);

    public SDEFile(Integer id, FileStoreNode parentNode) {
        this.parentNode = parentNode;
        String userHome = System.getProperty("user.home");

        File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
        this.file = new File(root, "sdeFiles/" + id + ".tmp");
        if (!file.exists()) {
            try {
                Boolean createResult = file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.id = id;
    }

    public SDEFile(Integer id, File file, FileStoreNode parentNode) {
        this.parentNode = parentNode;
        this.file = file;
        this.id = id;
    }

    public InputStream getInputStream() {
        try {
            if (file != null) {
                return new FileInputStream(file);
            }
        } catch (FileNotFoundException ex) {
            log.error("Count not find file", ex);
        }

        return null;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public File getFile() {
        return file;
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
        DataBank.saveSDEFile(this);
    }

    public FileStoreNode getParentNode() {
        return parentNode;
    }
}
