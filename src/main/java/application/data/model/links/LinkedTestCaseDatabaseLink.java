package application.data.model.links;

import application.data.model.DatabaseLink;
import application.node.implementations.TestCaseNode;
import application.node.implementations.TestManagerNode;
import application.node.objects.LinkedTestCase;

import java.util.UUID;

public class LinkedTestCaseDatabaseLink extends DatabaseLink {
    public LinkedTestCaseDatabaseLink() {
        super("linked_test_cases", LinkedTestCase.class);

        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("test_case_node_id", method("getTestCaseNodeUuid"), method("setTestCaseNode", TestCaseNode.class)); // 2
        link("test_manager_node_id", method("getTestManagerNodeUuid"), method("setTestManagerNode", TestManagerNode.class)); // 3
        link("linked_test_case_parent_id", method("getParentTestCaseUUID"), method("setParentTestCase", LinkedTestCase.class)); // 4
        link("enabled", method("isEnabled"), method("setEnabled", Boolean.class)); // 5
        link("type", method("getType"), method("setType", String.class)); // 6
    }
}