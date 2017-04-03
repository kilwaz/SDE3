package application.gui.columns.testsetbatchwindow.commandview;

import application.test.TestCommand;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.PropertyValueFactory;

public class CommandOrderColumn extends TableColumn<TestCommand, Integer> {
    public CommandOrderColumn() {
        setText("#");
        setPrefWidth(35);
        setCellValueFactory(new PropertyValueFactory<>("CommandOrder"));
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
                } else {
                    setText(null);
                }
            }
        });
    }
}
