package application.test;

import de.jensd.fx.fontawesome.AwesomeDude;
import de.jensd.fx.fontawesome.AwesomeIcon;
import javafx.scene.control.Label;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestStep {
    public static final Integer TEST_TYPE_EQUAL = 1;

    private static Logger log = Logger.getLogger(TestStep.class);

    private Integer id = -1;
    private String testString = "";
    private String expectedEqual = "";
    private String observedEqual = "";
    private Boolean successful = false;
    private BufferedImage screenshot = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private TestResult parentResult = null;
    private Integer testType = 0;

    public TestStep() {

    }

    public TestStep(Integer id, String testString, Boolean successful, BufferedImage screenshot, TestResult parentResult) {
        this.id = id;
        this.testString = testString;
        this.successful = successful;
        this.screenshot = screenshot;
        this.parentResult = parentResult;
    }

    // Returns an input stream from the current screenshot
    public InputStream getScreenshotInputStream() {
        InputStream inputStream = null;
        if (screenshot != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png", os);
                inputStream = new ByteArrayInputStream(os.toByteArray());
            } catch (IOException ex) {
                log.error(ex);
            }
        }

        return inputStream;
    }

    public Integer getTestResultId() {
        return parentResult.getId();
    }

    public Label getSuccessLabel() {
        if (successful) {
            return AwesomeDude.createIconLabel(AwesomeIcon.CHECK);
        } else {
            return AwesomeDude.createIconLabel(AwesomeIcon.CLOSE);
        }
    }

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(Boolean successful) {
        this.successful = successful;
    }

    public TestResult getParentResult() {
        return parentResult;
    }

    public void setParentResult(TestResult parentResult) {
        this.parentResult = parentResult;
    }

    public BufferedImage getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(BufferedImage screenshot) {
        this.screenshot = screenshot;
    }

    public String getTestString() {
        return testString;
    }

    public void setTestString(String testString) {
        this.testString = testString;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getExpectedEqual() {
        return expectedEqual;
    }

    public void setExpectedEqual(String expectedEqual) {
        this.expectedEqual = expectedEqual;
    }

    public String getObservedEqual() {
        return observedEqual;
    }

    public void setObservedEqual(String observedEqual) {
        this.observedEqual = observedEqual;
    }

    public Integer getTestType() {
        return testType;
    }

    public void setTestType(Integer testType) {
        this.testType = testType;
    }

    public String getTestTypeName() {
        switch (testType) {
            case 1:
                return "Equals";
            default:
                return "Unknown";
        }
    }
}
