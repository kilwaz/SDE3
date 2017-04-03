package sde.application.gui.columns.testsetbatchwindow.commandview;

import sde.application.test.TestCommand;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;

public class CommandLineNumberColumn extends TableColumn<TestCommand, Integer> {
    public CommandLineNumberColumn() {
        setText("Line #");
        setPrefWidth(50);
        setCellValueFactory(new PropertyValueFactory<>("CommandLineNumber"));
        setCellFactory(column -> new TableCell<TestCommand, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && item > 0) {
                    setText(item.toString());
                    if (!isEmpty()) {
                        TableRow<TestCommand> tableRow = getTableRow();
                        if (tableRow.getItem().hasException()) {
                            setStyle("-fx-background-color:salmon");
                        } else {
                            setStyle("");
                        }
                    }
                }
            }
        });
    }
}
