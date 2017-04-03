package sde.application.gui.columns.testsetbatchwindow.commandview;

import sde.application.test.TestCommand;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class RawCommandColumn extends TableColumn<TestCommand, String> {
    public RawCommandColumn() {
        setText("Raw");
        setPrefWidth(300);
        setCellValueFactory(new PropertyValueFactory<>("RawCommand"));
    }
}
