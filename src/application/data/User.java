package application.data;

import application.gui.Program;

public class User {
    private Integer id;
    private Integer lastProgram;
    private Program currentProgram;
    private String username;

    public User(Integer id, String username, Integer lastProgram) {
        this.id = id;
        this.username = username;
        this.lastProgram = lastProgram;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Program getCurrentProgram() {
        return currentProgram;
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
