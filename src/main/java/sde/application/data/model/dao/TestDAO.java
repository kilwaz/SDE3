package sde.application.data.model.dao;

import sde.application.data.UpdateQuery;

public class TestDAO {
    public TestDAO() {

    }

    public void deleteAllTests() {
        new UpdateQuery("update test set forDelete = 1").execute();
    }
}
