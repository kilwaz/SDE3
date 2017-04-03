package sde.application.net.ssh;

public class SSHCommand {
    private String command;
    private String returnString;
    private Integer timeout;

    public SSHCommand(String command, String returnString, Integer timeout) {
        this.command = command;
        this.returnString = returnString;
        this.timeout = timeout;
    }

    public String getCommand() {
        return command;
    }

    public String getReturnString() {
        return returnString;
    }

    public Integer getTimeout() {
        return timeout;
    }
}
