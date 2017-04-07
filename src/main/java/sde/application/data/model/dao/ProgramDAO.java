package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.data.User;
import sde.application.data.model.links.ProgramDatabaseLink;
import sde.application.gui.Program;

import java.util.ArrayList;
import java.util.List;

public class ProgramDAO {
    public ProgramDAO() {

    }

    public List<Program> getProgramsByUser(User user) {
        ProgramDatabaseLink programDatabaseLink = new ProgramDatabaseLink();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from " + programDatabaseLink.getTableName() + " where user_id = ?;")
                .addParameter(user.getUuidString())
                .execute();

        List<Program> programs = new ArrayList<>();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            programs.add(Program.load(DAO.UUIDFromString(uuid), Program.class));
        }

        return programs;
    }

    public Program getProgramByName(String programName) {
        ProgramDatabaseLink programDatabaseLink = new ProgramDatabaseLink();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from " + programDatabaseLink.getTableName() + " where name = ?;")
                .addParameter(programName)
                .execute();

        List<SelectResultRow> selectResultRows = selectResult.getResults();
        if (selectResultRows.size() > 0) {
            SelectResultRow resultRow = selectResultRows.get(0);
            String uuid = resultRow.getString("uuid");
            return Program.load(DAO.UUIDFromString(uuid), Program.class);
        }
        return null;
    }
}
