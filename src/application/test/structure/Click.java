package application.test.structure;

import application.test.TestCommand;
import application.utils.AppParams;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

public class Click extends BaseStructure {
    private static Logger log = Logger.getLogger(Click.class);

    private BaseTarget targetElement;

    public Click(TestCommand testCommand) {
        super(testCommand);
        this.targetElement = new BaseTarget(testCommand);
        this.setStructureType(STRUCTURE_CLICK);
    }

    public Node createStructureInterface() {
        HBox hBox = new HBox(5);

        Label targetLabel = new Label("Target:");
        targetLabel.setFont(AppParams.getFont(12));

        ChoiceBox<String> cb = new ChoiceBox<>(FXCollections.observableArrayList(BaseTarget.getNamedListOfTargetTypes()));
        cb.getSelectionModel().select(targetElement.getTypeInUse());

        TextField targetField = new TextField();
        targetField.setText(targetElement.getValueInUse());
        targetField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            targetElement.setValueInUse(textField.getText());
        });

        hBox.getChildren().addAll(targetLabel, cb, targetField);
        return hBox;
    }

    public BaseTarget getTargetElement() {
        return targetElement;
    }

    public String toScript() {
        String script = "click>";

        if (targetElement.isSet()) {
            script += targetElement.getTypeInUse() + "::" + targetElement.getValueInUse();
        }

        return script;
    }
}
