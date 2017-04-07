package sde.application.gui.columns.testsetbatchwindow.commandview;

import sde.application.gui.columns.testsetbatchwindow.commandview.cell.BooleanTableCell;
import sde.application.test.TestCommand;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class HasScreenshotColumn extends TableColumn<TestCommand, Boolean> {
    public HasScreenshotColumn() {
        setText("Screenshot");
        setPrefWidth(60);
        setCellValueFactory(new PropertyValueFactory<>("HasScreenshot"));

        Callback<TableColumn<TestCommand, Boolean>, TableCell<TestCommand, Boolean>> booleanCellFactory = p -> new BooleanTableCell();
        setCellFactory(booleanCellFactory);
    }
}
