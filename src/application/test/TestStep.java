package application.test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestStep {
    private Integer id = -1;
    private String testString = "";
    private Boolean successful = false;
    private BufferedImage screenshot = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    private TestResult parentResult = null;

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
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return inputStream;
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
}
