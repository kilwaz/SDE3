package sde.application.net.objects.messages.server;

import org.apache.log4j.Logger;
import sde.application.net.objects.NetworkObject;

public class RecordingPiece extends NetworkObject {

    private static Logger log = Logger.getLogger(RecordingPiece.class);

    private Integer pieceNumber = 0;
    private String fileReference = "";

    public RecordingPiece() {
        super();
    }

}
