package application.gui.columns.testsetbatchwindow.commandparamters;

import application.test.TestParameter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ParameterValueColumn extends TableColumn<TestParameter, String> {
    public ParameterValueColumn() {
        setText("Value");
        //setPrefWidth(100);
        setCellValueFactory(new PropertyValueFactory<>("ParameterValue"));
    }
}
