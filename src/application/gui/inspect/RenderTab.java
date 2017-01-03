package application.gui.inspect;

import application.net.proxy.RecordedRequest;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.IOException;

public class RenderTab extends Tab {
    private WebView browser;
    private WebEngine webEngine;

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
            ImageView imageView = new ImageView();
            imageView.setImage(new Image(recordedRequest.getResponseInputStream()));

            StackPane stackPane = new StackPane();
            stackPane.getChildren().add(imageView);
            StackPane.setAlignment(imageView, Pos.CENTER);
            this.setContent(stackPane);

//            browser = new WebView();
//            webEngine = browser.getEngine();
//
//            browser.setPrefHeight(Integer.MAX_VALUE);
//            browser.setPrefWidth(Integer.MAX_VALUE);
//
//            browser.setMaxHeight(Integer.MAX_VALUE);
//            browser.setMaxWidth(Integer.MAX_VALUE);
//
//
//
//            try {
//                String content = "<html><body><img alt=\"Embedded Image\" src=\"data:image/" + recordedRequest.getMediaSubType() + ";base64," + Base64.encodeBase64String(IOUtils.toByteArray(recordedRequest.getResponseInputStream())) + "\" /></body></html>";
//                webEngine.loadContent(content);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            this.setContent(browser);
        } else {
            StackPane stackPane = new StackPane();
            Label label = new Label("Nothing to show");
            stackPane.getChildren().add(label);
            StackPane.setAlignment(label, Pos.CENTER);
            this.setContent(stackPane);
        }
    }
}
