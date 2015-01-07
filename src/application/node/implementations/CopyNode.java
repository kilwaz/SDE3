package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class CopyNode extends DrawableNode {
    private String copyFrom = "";
    private String copyTo = "";

    // This will make a copy of the node passed to it
    public CopyNode(CopyNode copyNode) {
        this.setId(-1);
        this.setX(copyNode.getX());
        this.setY(copyNode.getY());
        this.setWidth(copyNode.getWidth());
        this.setHeight(copyNode.getHeight());
        this.setColor(copyNode.getColor());
        this.setScale(copyNode.getScale());
        this.setContainedText(copyNode.getContainedText());
        this.setProgramId(copyNode.getProgramId());
        this.setNextNodeToRun(copyNode.getNextNodeToRun());

        this.setCopyFrom(copyNode.getCopyFrom());
        this.setCopyTo(copyNode.getCopyTo());
    }

    public CopyNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public CopyNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public CopyNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        super(x, y, width, height, color, containedText, programId, id);
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        // The ordering here is Tab < ScrollPane < AnchorPane
        Tab tab = controller.createDefaultNodeTab(this);
        ScrollPane scrollPane = new ScrollPane();
        AnchorPane anchorPane = (AnchorPane) tab.getContent(); // We get the Anchor pane from the default Tab and change it to a ScrollPane

        scrollPane.setContent(anchorPane);

        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        HBox copyFromRow = new HBox(5);
        copyFromRow.setAlignment(Pos.CENTER);
        Label copyFromLabel = new Label();
        TextField copyFromField = new TextField();

        copyFromLabel.setText("Copy From:");
        copyFromLabel.setId("copyLabel-from-" + this.getId());
        copyFromLabel.setPrefWidth(80.0);

        copyFromField.setText(copyFrom);
        copyFromField.setId("copyField-from-" + this.getId());
        copyFromField.setPrefWidth(400.0);
        copyFromField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                copyFrom = textField.getText();

                DataBank.saveNode(this);
            }
        });

        copyFromRow.getChildren().add(copyFromLabel);
        copyFromRow.getChildren().add(copyFromField);

        HBox copyToRow = new HBox(5);
        copyToRow.setAlignment(Pos.CENTER);
        Label copyToLabel = new Label();
        TextField copyToField = new TextField();

        copyToLabel.setText("Copy To:");
        copyToLabel.setId("copyLabel-to-" + this.getId());
        copyToLabel.setPrefWidth(80.0);

        copyToField.setText(copyTo);
        copyToField.setId("copyField-to-" + this.getId());
        copyToField.setPrefWidth(400.0);
        copyToField.setOnAction(event -> {
            TextField textField = (TextField) event.getSource();
            if (!textField.getText().isEmpty()) {
                copyTo = textField.getText();

                DataBank.saveNode(this);
            }
        });

        copyToRow.getChildren().add(copyToLabel);
        copyToRow.getChildren().add(copyToField);

        rows.getChildren().add(copyFromRow);
        rows.getChildren().add(copyToRow);

        anchorPane.getChildren().add(rows);
        tab.setContent(scrollPane);

        return tab;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("CopyTo", copyTo.getClass().getName(), copyTo));
        savableAttributes.add(new SavableAttribute("CopyFrom", copyFrom.getClass().getName(), copyFrom));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
    }

    public String getCopyFrom() {
        return copyFrom;
    }

    public void setCopyFrom(String copyFrom) {
        this.copyFrom = copyFrom;
    }

    public String getCopyTo() {
        return copyTo;
    }

    public void setCopyTo(String copyTo) {
        this.copyTo = copyTo;
    }
}
