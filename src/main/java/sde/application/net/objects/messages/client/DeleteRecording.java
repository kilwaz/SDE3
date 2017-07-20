package sde.application.net.objects.messages.client;

import org.apache.log4j.Logger;
import sde.application.net.objects.NetworkObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeleteRecording extends NetworkObject {

    private static Logger log = Logger.getLogger(DeleteRecording.class);
    private String fileName = "";

    public DeleteRecording setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DeleteRecording() {
        super();
    }

    public void deleteFile() {
        if (fileName != null && !fileName.isEmpty()) {
            StringBuffer output = new StringBuffer();

            log.info("Removing file " + fileName);

            String command = "rm -f " + fileName;

            String[] args = new String[]{"/bin/bash", "-c", command};
            Process p = null;
            try {
                p = new ProcessBuilder(args).start();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (p != null) {
                    p.waitFor();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    log.info("Completed command");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
