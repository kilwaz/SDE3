package sde.application.test.action;

import org.apache.log4j.Logger;
import sde.application.test.TestParameter;

public class TestWebAction extends WebAction {
    private static Logger log = Logger.getLogger(TestWebAction.class);

    public TestWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestParameter outputFile = getTestCommand().getParameterByName("outputFile");
        TestParameter recordingFile = getTestCommand().getParameterByName("recordingFile");

        if (outputFile.exists()) {
            getRunningTest().setFileOutputPath(outputFile.getParameterValue());
        }
        if (recordingFile.exists()) {
            log.info("Setting local file recording location to " + recordingFile.getParameterValue());
            getRunningTest().setFileRecordingPath(recordingFile.getParameterValue());
        }
    }
}
