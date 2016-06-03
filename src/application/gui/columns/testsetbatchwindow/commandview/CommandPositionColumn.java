package application.gui.columns.testsetbatchwindow.commandview;

import application.test.TestCommand;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CommandPositionColumn extends TableColumn<TestCommand, Integer> {
    public CommandPositionColumn() {
        setText("#");
        setPrefWidth(35);
        setCellValueFactory(new PropertyValueFactory<>("CommandPosition"));
    }
}
