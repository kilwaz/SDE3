package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.data.User;
import application.data.model.links.UserDatabaseLink;
import application.error.Error;

public class UserDAO {
    public UserDAO() {
    }

    public User getUserFromUsername(String username) {
        UserDatabaseLink userDatabaseLink = new UserDatabaseLink();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from " + userDatabaseLink.getTableName() + " where username = ?")
                .addParameter(username)
                .execute();

        User user = null;
        if (selectResult.getResults().size() > 1) {
            Error.DUPLICATE_RESULTS.record().additionalInformation("Multiple users with username " + username).create();
            return null;
        }

        for (SelectResultRow resultRow : selectResult.getResults()) {
            String uuid = resultRow.getString("uuid");
            user = User.load(DAO.UUIDFromString(uuid), User.class);
        }

        return user;
    }
}
