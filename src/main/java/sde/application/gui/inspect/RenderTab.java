package sde.application.gui.inspect;

import sde.application.Main;
import sde.application.error.Error;
import sde.application.net.proxy.RecordedRequest;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;

import java.io.*;

public class RenderTab extends Tab {
    private static Logger log = Logger.getLogger(RenderTab.class);
    private WebView browser;
    private WebEngine webEngine;
    private ContextMenu imageViewSaveContext;

    public RenderTab(RecordedRequest recordedRequest) {
        this.setText("Render");
        this.setClosable(false);

        if (recordedRequest.getMediaSubType().equals("html")) {
            browser = new WebView();
            webEngine = browser.getEngine();

            webEngine.loadContent(recordedRequest.getResponse());

            browser.setPrefHeight(Integer.MAX_VALUE);
            browser.setPrefWidth(Integer.MAX_VALUE);

            browser.setMaxHeight(Integer.MAX_VALUE);
            browser.setMaxWidth(Integer.MAX_VALUE);

            this.setContent(browser);
        } else if (recordedRequest.getMediaGroup().equals("image")) {
            StackPane stackPane = new StackPane();
            ImageView imageView = new ImageView();
            imageView.setImage(new Image(recordedRequest.getResponseInputStream()));

            // Close the context menu if it is open
            stackPane.setOnMouseClicked(event -> {
                if (imageViewSaveContext != null) {
                    imageViewSaveContext.hide();
                }
            });

            // Create a context menu for saving the image
            stackPane.setOnContextMenuRequested(event -> {
                if (imageViewSaveContext != null) {
                    imageViewSaveContext.hide();
                }
                MenuItem menuItemSaveAs = new MenuItem("Save as...");
                menuItemSaveAs.setOnAction(event1 -> {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save image as...");
                    fileChooser.setInitialFileName(recordedRequest.getFileNameFromURL());
                    OutputStream os = null;
                    InputStream is = recordedRequest.getResponseInputStream();
                    try {
                        File saveFile = fileChooser.showSaveDialog(Main.getInstance().getMainStage());
                        os = new FileOutputStream(saveFile);
                        IOUtils.copy(recordedRequest.getResponseInputStream(), os);
                    } catch (FileNotFoundException ex) {
                        Error.SDE_FILE_NOT_FOUND.record().create(ex);
                    } catch (IOException ex) {
                        Error.WRITE_FILE.record().create(ex);
                    } finally {
                        try {
                            if (os != null) {
                                os.close();
                            }
                            if (is != null) {
                                is.close();
                            }
                        } catch (IOException ex) {
                            Error.CLOSE_FILE_STREAM.record().create(ex);
                        }
                    }
                });

                imageViewSaveContext = new ContextMenu();
                imageViewSaveContext.getItems().add(menuItemSaveAs);
                imageViewSaveContext.show(stackPane, event.getScreenX(), event.getScreenY());
            });

            stackPane.getChildren().add(imageView);
            StackPane.setAlignment(imageView, Pos.CENTER);
            this.setContent(stackPane);
        } else {
            StackPane stackPane = new StackPane();
            Label label = new Label("Nothing to show");
            stackPane.getChildren().add(label);
            StackPane.setAlignment(label, Pos.CENTER);
            this.setContent(stackPane);
        }
    }
}
