package application.data.model.links;

import application.data.model.DatabaseLink;
import application.test.core.TestCase;

public class TestCaseDatabaseLink extends DatabaseLink {
    public TestCaseDatabaseLink() {
        super("test_case", TestCase.class);
    }
}
