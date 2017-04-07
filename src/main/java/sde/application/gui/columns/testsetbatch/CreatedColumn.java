package sde.application.gui.columns.testsetbatch;

import sde.application.test.core.TestSetBatch;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class CreatedColumn extends TableColumn {

    public CreatedColumn() {
        setText("Created");
        setPrefWidth(140);
        setCellValueFactory(new PropertyValueFactory<TestSetBatch, String>("FormattedTime"));
        setCellFactory(column -> new TableCell<TestSetBatch, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    setText(item);
                } else {
                    setText("");
                }
            }
        });
    }
}

