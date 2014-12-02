package application.data;

import javafx.scene.paint.Color;

import java.util.HashMap;

public class NodeColours {
    HashMap<String, NodeColour> nodeColours = new HashMap<>();

    public NodeColours() {
        DataBank.loadNodeColours(this);
    }

    public void addNodeColour(NodeColour nodeColour) {
        nodeColours.put(nodeColour.getNodeType(), nodeColour);
    }

    public NodeColour getNodeColour(String nodeType) {
        NodeColour nodeColour = nodeColours.get(nodeType);
        if (nodeColour == null) {
            nodeColour = new NodeColour(Color.WHITE, "TempNode");
        }
        return nodeColour;
    }
}
