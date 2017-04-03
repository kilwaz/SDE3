package sde.application.utils.managers;

import sde.application.data.Session;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static SessionManager sessionManager;
    private List<Session> sessions;

    private static Logger log = Logger.getLogger(SessionManager.class);

    public SessionManager() {
        sessionManager = this;
        sessions = new ArrayList<>();
    }

    public Session getCurrentSession() {
        if (sessions.size() > 0) {
            return sessions.get(0);
        }
        return null;
    }

    public void addSession(Session session) {
        sessions.add(session);
    }

    public static SessionManager getInstance() {
        return sessionManager;
    }

    public void clearAllSessions() {
        sessions.clear();
    }
}
