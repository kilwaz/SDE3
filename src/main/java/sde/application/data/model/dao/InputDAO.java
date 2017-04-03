package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.node.implementations.InputNode;
import sde.application.node.objects.Input;

import java.util.ArrayList;
import java.util.List;

public class InputDAO {
    public InputDAO() {

    }

    public List<Input> getInputsByNode(InputNode inputNode) {
        List<Input> inputs = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from input where node_id = ?")
                .addParameter(inputNode.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            inputs.add(Input.load(DAO.UUIDFromString(uuid), Input.class));
        }

        return inputs;
    }
}
