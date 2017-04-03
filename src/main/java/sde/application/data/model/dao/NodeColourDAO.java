package sde.application.data.model.dao;

import sde.application.data.NodeColour;
import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;

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
