package application.data.model.dao;

import application.data.UpdateQuery;

public class TestDAO {
    public TestDAO() {

    }

    public void deleteAllTests() {
        new UpdateQuery("delete from test").execute();
    }
}
