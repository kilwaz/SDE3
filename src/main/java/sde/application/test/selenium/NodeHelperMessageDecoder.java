package sde.application.test.selenium;

import org.apache.log4j.Logger;

public class NodeHelperMessageDecoder {

    private static Logger log = Logger.getLogger(NodeHelperMessageDecoder.class);
    private String currentMessage = "";
    private String currentResponse = "";

    public NodeHelperMessageDecoder(String inputLine) {
        this.currentMessage = inputLine;
        log.info("GOT: " + inputLine);
        processMessage();
    }

    private void processMessage() {
        if ("GREETINGS".equals(currentMessage)) {
            currentResponse = "Bye.";
        }
    }

    public String getCurrentResponse() {
        return currentResponse + "\n";
    }

    public Boolean hasResponse() {
        return !currentResponse.isEmpty();
    }

    public Boolean isGoodBye() {
        return "Bye.".equals(currentMessage);
    }
}
