package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.node.implementations.InputNode;
import application.node.objects.Input;

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
