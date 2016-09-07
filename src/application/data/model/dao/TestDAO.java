package application.data.model.dao;

import application.data.UpdateQuery;

public class TestDAO {
    public TestDAO() {

    }

    public void deleteAllTests() {
        new UpdateQuery("update test set forDelete = 1").execute();
    }
}
