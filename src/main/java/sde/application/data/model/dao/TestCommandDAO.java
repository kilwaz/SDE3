package sde.application.data.model.dao;

import sde.application.data.UpdateQuery;
import org.apache.log4j.Logger;

public class TestCommandDAO {
    private static Logger log = Logger.getLogger(TestCommandDAO.class);

    public TestCommandDAO() {
    }

    public void deleteAllTestCommands() {
        new UpdateQuery("update test_command set forDelete = 1").execute();
    }
}
