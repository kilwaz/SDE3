package sde.application.data.processes;

import org.apache.log4j.Logger;
import sde.application.data.SelectQuery;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;

import java.util.List;

public class OptimiseTables extends SDERunnable {
    private static Logger log = Logger.getLogger(OptimiseTables.class);

    public OptimiseTables() {

    }

    public static void execute() {
        new SDEThread(new OptimiseTables(), "Optimise tables", "", true);
    }

    public void threadRun() {
        List<String> tableNames = GetTableNamesList.getTableNamesList();
        for (String tableName : tableNames) {
            log.info("Optimising table " + tableName);
            SelectQuery selectQuery = new SelectQuery("optimize table " + tableName + ";");
            selectQuery.execute();
        }
        log.info("All tables optimised");
    }
}
