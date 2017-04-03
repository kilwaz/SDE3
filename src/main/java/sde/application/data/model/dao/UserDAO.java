package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.data.User;
import sde.application.data.model.links.UserDatabaseLink;
import sde.application.error.Error;

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
