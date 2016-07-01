package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.data.model.links.LinkedTestCaseDatabaseLink;
import application.node.implementations.TestManagerNode;
import application.node.objects.LinkedTestCase;

import java.util.ArrayList;
import java.util.List;

public class LinkedTestCaseDAO {
    public LinkedTestCaseDAO() {

    }

    public List<LinkedTestCase> getLinkedTestCases(TestManagerNode testManagerNode) {
        LinkedTestCaseDatabaseLink linkedTestCaseDatabaseLink = new LinkedTestCaseDatabaseLink();
        List<LinkedTestCase> linkedTestCases = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from " + linkedTestCaseDatabaseLink.getTableName() + " where test_manager_node_id = ?")
                .addParameter(testManagerNode.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            linkedTestCases.add(LinkedTestCase.load(DAO.UUIDFromString(uuid), LinkedTestCase.class));
        }

        return linkedTestCases;
    }

    public List<LinkedTestCase> getChildLinkedTestCases(LinkedTestCase linkedTestCase){
        LinkedTestCaseDatabaseLink linkedTestCaseDatabaseLink = new LinkedTestCaseDatabaseLink();
        List<LinkedTestCase> linkedTestCases = new ArrayList<>();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from " + linkedTestCaseDatabaseLink.getTableName() + " where linked_test_case_parent_id = ?")
                .addParameter(linkedTestCase.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            linkedTestCases.add(LinkedTestCase.load(DAO.UUIDFromString(uuid), LinkedTestCase.class));
        }

        return linkedTestCases;
    }
}
