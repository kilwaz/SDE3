package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.data.User;
import application.data.model.links.ProgramDatabaseLink;
import application.gui.Program;
import javafx.collections.transformation.SortedList;

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
}
