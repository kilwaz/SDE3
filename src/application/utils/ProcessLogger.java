package application.utils;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ProcessLogger extends SDERunnable {
    private static Logger log = Logger.getLogger(ProcessLogger.class);

    private InputStream logStream;
    private String processReference;

    public ProcessLogger(InputStream logStream, String processReference) {
        this.logStream = logStream;
        this.processReference = processReference;
    }

    public void threadRun() {
        try {
            InputStreamReader isr = new InputStreamReader(logStream);
            BufferedReader br = new BufferedReader(isr);
            String line;
            while ((line = br.readLine()) != null)
                log.info(processReference + " > " + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
