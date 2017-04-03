package application.data.model.dao;


import application.data.*;
import application.data.model.links.SavableAttributeDatabaseLink;
import application.node.design.DrawableNode;

import java.util.ArrayList;
import java.util.List;

public class SavableAttributeDAO {
    public SavableAttributeDAO() {

    }

    public List<SavableAttribute> getAttributes(DrawableNode drawableNode) {
        SavableAttributeDatabaseLink savableAttributeDatabaseLink = new SavableAttributeDatabaseLink();

        List<SavableAttribute> savableAttributes = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from " + savableAttributeDatabaseLink.getTableName() + " where node_id = ?")
                .addParameter(drawableNode.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            SavableAttribute savableAttribute = SavableAttribute.load(DAO.UUIDFromString(resultRow.getString("uuid")), SavableAttribute.class);
            savableAttributes.add(savableAttribute);
        }

        return savableAttributes;
    }

    public void deletePreviousRecord(SavableAttribute savableAttribute) {
        UpdateQuery delete = new UpdateQuery("delete from node_details where node_id = ? and object_name = ?");
        delete.addParameter(savableAttribute.getParentUuid())
                .addParameter(savableAttribute.getVariableName());
        delete.execute();
    }
}
