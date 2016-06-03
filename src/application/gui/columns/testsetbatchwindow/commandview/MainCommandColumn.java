package application.gui.columns.testsetbatchwindow.commandview;

import application.test.TestCommand;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainCommandColumn extends TableColumn<TestCommand, String> {
    public MainCommandColumn() {
        setText("Type");
        setPrefWidth(60);
        setCellValueFactory(new PropertyValueFactory<>("MainCommand"));
    }
}
