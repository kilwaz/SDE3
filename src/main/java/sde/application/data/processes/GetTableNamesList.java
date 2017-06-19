package sde.application.data.processes;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;

import java.util.ArrayList;
import java.util.List;

public class GetTableNamesList {

    public static List<String> getTableNamesList() {
        SelectQuery selectQuery = new SelectQuery("show tables;");

        SelectResult selectResult = (SelectResult) selectQuery.execute();

        List<String> tableNames = new ArrayList<>();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            tableNames.add(resultRow.getString(0));
        }

        return tableNames;
    }
}
