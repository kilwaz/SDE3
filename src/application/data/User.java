package application.data;

import application.data.model.DatabaseObject;
import application.gui.Program;

import java.util.UUID;

public class User extends DatabaseObject {
    private Program currentProgram;
    private String username;

    public User() {
        super();
    }

    public User(UUID uuid) {
        super(uuid);
    }

    public Program getCurrentProgram() {
        return currentProgram;
    }

    public UUID getCurrentProgramUuid() {
        if (currentProgram != null) {
            return currentProgram.getUuid();
        }
        return null;
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
}
