package sde.application.test.selenium;

import org.apache.log4j.Logger;

public class NodeHelperMessageDecoder {

    private static Logger log = Logger.getLogger(NodeHelperMessageDecoder.class);
    private String currentMessage = "";
    private String currentResponse = "";
    private String currentCommand = "";
    private String currentLogMessage = "";
    private Boolean isInitialiseFromServer = false;

    public NodeHelperMessageDecoder(String inputLine) {
        this.currentMessage = inputLine;
        log.info("GOT: " + inputLine);
        processMessage();
    }

    private void processMessage() {
        if (currentMessage.startsWith("ClientHello")) {
            log.info("Hello from client");
            currentResponse = "ServerHello";
        } else if (currentMessage.startsWith("ServerHello")) {
            isInitialiseFromServer = true;
            log.info("Hello from server");
        } else if (currentMessage.startsWith("COMMAND")) {
            currentCommand = currentMessage.substring(7);
        }
    }

    public String getCurrentResponse() {
        return currentResponse + "\n";
    }

    public String getCurrentCommand() {
        return currentCommand;
    }

    public Boolean hasResponse() {
        return !currentResponse.isEmpty();
    }

    public Boolean hasCommand() {
        return !currentCommand.isEmpty();
    }

    public Boolean isGoodBye() {
        return "Bye.".equals(currentMessage);
    }

    public Boolean isInitialiseFromServer() {
        return isInitialiseFromServer;
    }
}
