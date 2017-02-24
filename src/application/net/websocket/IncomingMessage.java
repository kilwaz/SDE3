package application.net.websocket;

import application.error.Error;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.net.SocketException;

public class IncomingMessage {
    private static Logger log = Logger.getLogger(IncomingMessage.class);
    private String finalMessage = "";
    private boolean inputOpen = true;

    public IncomingMessage(InputStream inputStream) {
        boolean inMessage = false;
        boolean seenSize = false;
        boolean withinKey = false;
        boolean withinMessageContent = false;
        boolean withinLength = false;
        int keyCounter = 0;
        int messageCounter = 0;
        int lengthOfMessage = 0;
        int lengthCounter = 0;
        int targetLengthCounter = 0;

        byte[] key = new byte[4];
        byte[] encodedMessage = null;
        byte[] decodedMessage = null;
        byte[] length = null;

        boolean readingMessage = true;

        try {
            while (readingMessage) {
                int i = inputStream.read();
                //log.info("-> " + i);

                if (!inMessage) {
                    if (i == 129) { // Text
                        //log.info("New text message");
                        inMessage = true;
                    }
                } else if (!seenSize) {
                    seenSize = true;
                    int sizeCheck = i - 128;
                    if (sizeCheck < 126) { // Size is actual number
                        lengthOfMessage = sizeCheck;
                        //log.info("Length is " + lengthOfMessage);
                        encodedMessage = new byte[lengthOfMessage];
                        decodedMessage = new byte[lengthOfMessage];
                        withinKey = true;
                    } else if (sizeCheck == 126) {  // Next 2 bytes are the number
                        withinLength = true;
                        targetLengthCounter = 2;
                        lengthCounter = 0;
                        length = new byte[2];
                        //log.info("Length requires next 2 bytes");
                    } else if (sizeCheck == 127) { // Next 8 bytes are the number
                        withinLength = true;
                        targetLengthCounter = 8;
                        lengthCounter = 0;
                        length = new byte[8];
                        //log.info("Length requires next 8 bytes");
                    }
                } else if (withinLength) {
                    length[lengthCounter] = (byte) i;
                    lengthCounter++;
                    if (lengthCounter == targetLengthCounter) {
                        if (targetLengthCounter == 2) { // Work out a 2 byte length
                            lengthOfMessage = ((length[0] & 0xff) << 8) | (length[1] & 0xff);
                        } else if (targetLengthCounter == 8) { // Work out an 8 byte length
                            long value = 0;
                            for (int n = 0; n < length.length; n++) {
                                value = (value << 8) + (length[n] & 0xff);
                            }
                            lengthOfMessage = new Long(value).intValue();
                        }
                        //log.info("Length is " + lengthOfMessage);
                        encodedMessage = new byte[lengthOfMessage];
                        decodedMessage = new byte[lengthOfMessage];
                        withinKey = true;
                        withinLength = false;
                    }
                } else if (withinKey) { // Get the decode key
                    key[keyCounter] = (byte) i;
                    keyCounter++;
                    if (keyCounter == 4) {
                        withinKey = false;
                        withinMessageContent = true;
                    }
                } else if (withinMessageContent) { // Get the whole message
                    encodedMessage[messageCounter] = (byte) i;
                    messageCounter++;
                    if (messageCounter >= lengthOfMessage) {
                        //log.info("That's everything!");

                        for (int n = 0; n < encodedMessage.length; n++) {
                            decodedMessage[n] = (byte) (encodedMessage[n] ^ key[n & 0x3]);
                        }

                        finalMessage = new String(decodedMessage, "UTF-8");

                        //log.info("Final string = " + finalMessage);

                        readingMessage = false;

                        // This is the end of the message, so reset everything back to false
                        withinMessageContent = false;
                        inMessage = false;
                        seenSize = false;
                        key = new byte[4];
                        encodedMessage = null;
                        decodedMessage = null;
                        length = null;
                        keyCounter = 0;
                        messageCounter = 0;
                        lengthOfMessage = 0;
                        lengthCounter = 0;
                        targetLengthCounter = 0;
                    }
                } else if (i == -1) {
                    log.info("Lost connection");
                    readingMessage = false;
                    inputOpen = false;
                } else {
                    //log.info("WTF is this -> " + i);
                }
            }
        } catch (SocketException ex) {
            inputOpen = false;
        } catch (Exception ex) {
            Error.WEBSOCKET_EXCEPTION.record().create(ex);
            inputOpen = false;
        }
    }

    public boolean isInputOpen() {
        return inputOpen;
    }

    public String getFinalMessage() {
        return finalMessage;
    }
}
