package application.gui.columns.testsetbatch;

import application.test.core.TestSetBatch;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class CreatedColumn extends TableColumn {
    private static DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("kk:mm:ss dd MMM yyyy");

    public CreatedColumn() {
        setText("Created");
        setPrefWidth(140);
        setCellValueFactory(new PropertyValueFactory<TestSetBatch, DateTime>("CreatedTime"));
        setCellFactory(column -> new TableCell<TestSetBatch, DateTime>() {
            @Override
            protected void updateItem(DateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null) {
                    String formattedItemStr = dateFormatter.print(item);
                    setText(formattedItemStr);
                } else {
                    setText("");
                }
            }
        });
    }

}

