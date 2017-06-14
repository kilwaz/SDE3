package sde.application.data.model.dao;

import org.apache.log4j.Logger;
import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.data.model.links.TestDatabaseLink;
import sde.application.node.objects.Test;

import java.util.ArrayList;
import java.util.List;

public class TestDAO {
    private static Logger log = Logger.getLogger(TestDAO.class);

    public TestDAO() {

    }

    public List<Test> getAllTests() {
        return getAllTests(-1);
    }

    public List<Test> getAllTests(Integer limitedTo) {
        TestDatabaseLink testDatabaseLink = new TestDatabaseLink();

        String limitedToStr = "";
        if (limitedTo > -1) {
            limitedToStr = " limit " + limitedTo;
        }

        SelectQuery selectQuery = new SelectQuery("select uuid from " + testDatabaseLink.getTableName() + limitedToStr);
        SelectResult selectResult = (SelectResult) selectQuery.execute();

        List<Test> tests = new ArrayList<>();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            log.info("Loading " + uuid);
            tests.add(Test.load(DAO.UUIDFromString(uuid), Test.class));
        }

        return tests;
    }
}
