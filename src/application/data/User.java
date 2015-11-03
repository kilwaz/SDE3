package application.data;

import application.data.model.DatabaseObject;
import application.gui.Program;

public class User extends DatabaseObject {
    private Integer lastProgram;
    private Program currentProgram;
    private String username;

    public User(Integer id, String username, Integer lastProgram) {
        super(id);
        this.username = username;
        this.lastProgram = lastProgram;
    }

    public Program getCurrentProgram() {
        return currentProgram;
    }

    public Integer getCurrentProgramId() {
        if (currentProgram != null) {
            return currentProgram.getId();
        } else {
            return -1;
        }
    }

    public void setCurrentProgram(Program currentProgram) {
        this.currentProgram = currentProgram;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getLastProgram() {
        return lastProgram;
    }

    public void setLastProgram(Integer lastProgram) {
        this.lastProgram = lastProgram;
    }
}
