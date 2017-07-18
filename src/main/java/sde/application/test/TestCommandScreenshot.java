package sde.application.test;

import sde.application.data.model.DatabaseObject;
import sde.application.data.model.dao.TestCommandScreenshotDAO;
import sde.application.error.Error;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TestCommandScreenshot extends DatabaseObject {
    private BufferedImage screenshot = null;
    private TestCommand parent;

    public TestCommandScreenshot() {
        super();
    }

    // This shouldn't be used to retrieve a screenshot
    public BufferedImage getScreenshot() {
        return screenshot;
    }

    public void setScreenshot(BufferedImage screenshot) {
        this.screenshot = screenshot;
        this.save();
        this.screenshot = null;
    }

    // Returns an input stream from the current screenshot
    public InputStream getScreenshotInputStream() {
        InputStream inputStream = null;
        // First see if the object already has a screenshot available
        BufferedImage screenshot = this.screenshot;
        if (screenshot == null) { // If not we keystore the screenshot from the database
            screenshot = getScreenshotFromDatabase();
        }

        if (screenshot != null) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png", os);
                inputStream = new ByteArrayInputStream(os.toByteArray());
            } catch (IOException ex) {
                Error.RETRIEVE_SCREENSHOT.record().create(ex);
            }
        }

        return inputStream;
    }

    public BufferedImage getScreenshotFromDatabase() {
        TestCommandScreenshotDAO testCommandScreenshotDAO = new TestCommandScreenshotDAO();
        return testCommandScreenshotDAO.getLazyScreenshot(this);
    }

    public TestCommand getParent() {
        return parent;
    }

    public void setParentTestCommand(TestCommand parent) {
        this.parent = parent;
    }

    public String getParentTestCommandUuid() {
        if (parent != null) {
            return parent.getUuidString();
        }
        return null;
    }
}
