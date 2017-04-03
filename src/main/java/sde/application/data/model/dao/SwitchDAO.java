package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.node.implementations.SwitchNode;
import sde.application.node.objects.Switch;

import java.util.ArrayList;
import java.util.List;

public class SwitchDAO {
    public SwitchDAO() {

    }

    public List<Switch> getSwitchesByNode(SwitchNode switchNode) {
        List<Switch> switches = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from switch where node_id = ?")
                .addParameter(switchNode.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            switches.add(Switch.load(DAO.UUIDFromString(uuid), Switch.class));
        }

        return switches;
    }
}
