package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.error.Error;
import sde.application.test.TestCommandScreenshot;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TestCommandScreenshotDAO {
    private static Logger log = Logger.getLogger(TestCommandScreenshotDAO.class);

    public TestCommandScreenshotDAO() {

    }

    public BufferedImage getLazyScreenshot(TestCommandScreenshot testCommandScreenshot) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select screenshot from test_command_screenshot where uuid = ?")
                .addParameter(testCommandScreenshot.getUuidString())
                .execute();

        BufferedImage bufferedImage = null;

        for (SelectResultRow resultRow : selectResult.getResults()) {
            InputStream screenshotInputStream = resultRow.getBlobInputStream("screenshot");
            try {
                bufferedImage = ImageIO.read(screenshotInputStream);
            } catch (IOException ex) {
                Error.LOAD_BUFFERED_IMAGE_FAILED.record().create(ex);
                ex.printStackTrace();
            }
        }

        return bufferedImage;
    }
}
