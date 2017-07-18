package sde.application.net.objects.messages.client;

import sde.application.net.objects.NetworkObject;
import sde.application.net.objects.messages.server.Recording;

import java.util.ArrayList;
import java.util.List;

public class RetrieveRecording extends NetworkObject {
    private String recordingReference = "";

    public RetrieveRecording() {
        super();
    }

    public RetrieveRecording setRecordingReference(String recordingReference) {
        this.recordingReference = recordingReference;
        return this;
    }

    public List<NetworkObject> getResponses() {
        List<NetworkObject> networkObjects = new ArrayList<>();
        Recording recording = new Recording().setRecordingFileName(recordingReference);
        recording.loadFile();
        networkObjects.add(recording);
        return networkObjects;
    }
}
