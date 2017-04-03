package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.node.implementations.TriggerNode;
import application.node.objects.Trigger;

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
