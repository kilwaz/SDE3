package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.data.UpdateQuery;
import application.error.Error;
import application.test.TestCommand;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TestCommandDAO {
    private static Logger log = Logger.getLogger(TestCommandDAO.class);

    public TestCommandDAO() {

    }

    public BufferedImage getLazyScreenshot(TestCommand testCommand) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select screenshot from test_command where uuid = ?")
                .addParameter(testCommand.getUuidString())
                .execute();

        BufferedImage imBuff = null;

        for (SelectResultRow resultRow : selectResult.getResults()) {
            InputStream screenshotInputStream = resultRow.getBlobInputStream("screenshot");
            try {
                imBuff = ImageIO.read(screenshotInputStream);
            } catch (IOException ex) {
                Error.LOAD_BUFFERED_IMAGE_FAILED.record().create(ex);
                ex.printStackTrace();
            }
        }

        return imBuff;
    }

    public void deleteAllTestCommands() {
        new UpdateQuery("update test_command set forDelete = 1").execute();
    }
}
