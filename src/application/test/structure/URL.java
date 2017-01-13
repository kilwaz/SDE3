package application.test.structure;

import application.test.TestCommand;
import application.test.TestParameter;
import application.utils.AppParams;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

public class URL extends BaseStructure {
    private static Logger log = Logger.getLogger(URL.class);

    private String url = "";

    public URL(TestCommand testCommand) {
        super(testCommand);
        TestParameter urlParam = testCommand.getParameterByName("url");
        this.url = urlParam.getParameterValue();
        this.setStructureType(STRUCTURE_URL);
    }

    public Node createStructureInterface() {
        HBox hBox = new HBox(5);
        hBox.setAlignment(Pos.CENTER_LEFT);

        Label gotoLabel = new Label(" go to ");
        gotoLabel.setFont(AppParams.getFont(12));

        TextField urlField = new TextField();
        urlField.setText(url);
        urlField.setPrefWidth(400d);
        urlField.setOnKeyReleased(event -> {
            TextField textField = (TextField) event.getSource();
            url = textField.getText();
        });

        hBox.getChildren().addAll(gotoLabel, urlField);
        return hBox;
    }

    public String toScript() {
        String script = "url>";

        script += "url::" + url;

        return script;
    }
}
