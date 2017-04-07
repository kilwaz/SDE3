package sde.application.data.model.links;

import sde.application.data.NodeColour;
import sde.application.data.model.DatabaseLink;

public class NodeColourDatabaseLink extends DatabaseLink {
    public NodeColourDatabaseLink() {
        super("node_colour", NodeColour.class);

        // Make sure the order is the same as column order in database
        link("uuid", method("getUuidString"), method("setUuidFromString", String.class)); // 1
        link("node_type", method("getNodeType"), method("setNodeType", String.class)); // 2
        link("colour_r", method("getRed"), method("setRed", Integer.class)); // 3
        link("colour_g", method("getGreen"), method("setGreen", Integer.class)); // 4
        link("colour_b", method("getBlue"), method("setBlue", Integer.class)); // 5
    }
}
