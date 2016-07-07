package application.test;

import org.joda.time.DateTime;

public class TestCommandItem {
    private String rawCommand = "";
    private String mainCommand = "";
    private Integer commandPosition;
    private DateTime startDate;
    private Boolean hasScreenshot;

    public TestCommandItem(TestCommand testCommand) {
        this.rawCommand = testCommand.getRawCommand();
        this.commandPosition = testCommand.getCommandLineNumber();
        this.mainCommand = testCommand.getMainCommand();
        this.startDate = testCommand.getCommandDate();
        this.hasScreenshot = testCommand.getHasScreenshot();
    }

    public String getRawCommand() {
        return rawCommand;
    }

    public void setRawCommand(String rawCommand) {
        this.rawCommand = rawCommand;
    }

    public String getMainCommand() {
        return mainCommand;
    }

    public void setMainCommand(String mainCommand) {
        this.mainCommand = mainCommand;
    }

    public Integer getCommandPosition() {
        return commandPosition;
    }

    public void setCommandPosition(Integer commandPosition) {
        this.commandPosition = commandPosition;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public Boolean getHasScreenshot() {
        return hasScreenshot;
    }

    public void setHasScreenshot(Boolean hasScreenshot) {
        this.hasScreenshot = hasScreenshot;
    }
}