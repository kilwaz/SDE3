package application.data.model.links;

import application.data.NodeColour;
import application.data.model.DatabaseLink;
import application.error.Error;

public class NodeColourDatabaseLink extends DatabaseLink {
    public NodeColourDatabaseLink() {
        super("node_colour");
        try {
            // Make sure the order is the same as column order in database
            link("node_type", NodeColour.class.getMethod("getNodeType")); // 1
            link("colour_r", NodeColour.class.getMethod("getRed")); // 2
            link("colour_g", NodeColour.class.getMethod("getGreen")); // 3
            link("colour_b", NodeColour.class.getMethod("getBlue")); // 4
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }
    }
}
