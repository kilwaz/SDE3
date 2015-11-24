package application.data.model.dao;

import application.data.NodeColour;
import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;

import java.util.ArrayList;
import java.util.List;

public class NodeColourDAO {
    public NodeColourDAO() {

    }

    public List<NodeColour> getNodeColours() {
        List<NodeColour> nodeColours = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from node_colour")
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            nodeColours.add(NodeColour.load(DAO.UUIDFromString(uuid), NodeColour.class));
        }

        return nodeColours;
    }
}
