package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.node.implementations.TriggerNode;
import sde.application.node.objects.Trigger;

import java.util.ArrayList;
import java.util.List;

public class TriggerDAO {
    public TriggerDAO() {

    }

    public List<Trigger> getTriggersByNode(TriggerNode triggerNode) {
        List<Trigger> triggers = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from trigger_condition where node_id = ?")
                .addParameter(triggerNode.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            triggers.add(Trigger.load(DAO.UUIDFromString(uuid), Trigger.class));
        }

        return triggers;
    }
}
