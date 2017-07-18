package sde.application.net.objects.messages.client;

import org.apache.log4j.Logger;
import sde.application.net.objects.NetworkObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Command extends NetworkObject {

    private static Logger log = Logger.getLogger(Command.class);

    private String command = "";

    public Command() {
        super();
    }

    public Command setCommand(String command) {
        this.command = command;
        return this;
    }

    public String getCommand() {
        return command;
    }

    public void runCommand() {
        log.info("Running command ... " + command);

        StringBuffer output = new StringBuffer();

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
