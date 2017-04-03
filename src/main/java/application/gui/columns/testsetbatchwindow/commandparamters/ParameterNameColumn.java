package application.gui.columns.testsetbatchwindow.commandparamters;

import application.test.TestParameter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ParameterNameColumn extends TableColumn<TestParameter, String> {
    public ParameterNameColumn() {
        setText("Name");
        //setPrefWidth(100);
        setCellValueFactory(new PropertyValueFactory<>("ParameterName"));
    }
}
