package sde.application.data.model.links;

import sde.application.data.model.DatabaseLink;
import sde.application.test.core.TestCase;

public class TestCaseDatabaseLink extends DatabaseLink {
    public TestCaseDatabaseLink() {
        super("test_case", TestCase.class);
    }
}
