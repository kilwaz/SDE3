package sde.application.data;

import sde.application.gui.Program;

public class Session {
    private User user;
    private Program selectedProgram;

    public Session() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Program getSelectedProgram() {
        return selectedProgram;
    }

    public void setSelectedProgram(Program selectedProgram) {
        this.selectedProgram = selectedProgram;
    }
}
