package application.data.imports;

import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.concurrent.Task;
import javafx.scene.Node;

public class ImportTask extends Task<Integer> {
    private Integer progress = 0;
    private Integer maximum = 0;
    private Boolean isFinished = false;
    private Boolean waitingToStart = true;
    private String xml = "";
    private Node statusImage = null;

    public ImportTask(String xml, String fileName) {
        this.xml = xml;
        statusImage = GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.FILE_TEXT_ALT).build();
        updateTitle(fileName);
    }

    public void updateProgress(Integer progress) {
        this.progress = progress;
    }

    public void started() {
        this.waitingToStart = false;
    }

    public void setIsFinished(Boolean isFinished) {
        this.isFinished = isFinished;
    }

    public void setMaximum(Integer maximum) {
        this.maximum = maximum;
    }

    public String getXml() {
        return xml;
    }

    public Node getStatusImage() {
        return statusImage;
    }

    @Override
    protected Integer call() throws Exception {
        while (!isFinished) {
            if (isCancelled()) {
                updateMessage("Cancelled");
                isFinished = true;
                break;
            }

            if (waitingToStart) {
                updateMessage("Waiting to start...");
                updateProgress(0, 1);
            } else {
                updateMessage("Nodes imported " + progress + "/" + maximum);
                updateProgress(progress, maximum);
            }

            // Now block the thread for a short time, but be sure
            // to check the interrupted exception for cancellation!
            try {
                Thread.sleep(100);
            } catch (InterruptedException interrupted) {
                if (isCancelled()) {
                    updateMessage("Cancelled");
                    isFinished = true;
                    break;
                }
            }
        }
        return progress;
    }
}
