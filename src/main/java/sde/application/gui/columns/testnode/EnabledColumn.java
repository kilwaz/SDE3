package sde.application.gui.columns.testnode;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TreeTableColumn;

public class EnabledColumn extends TreeTableColumn<LinkedTestCaseTreeObject, Boolean> {
    public EnabledColumn() {
        setText("Enabled");
        setPrefWidth(130);
        setCellValueFactory(p -> {
            LinkedTestCaseTreeObject linkedTestCaseTreeObject = p.getValue().getValue();
            if (linkedTestCaseTreeObject.getType().equals(LinkedTestCaseTreeObject.LINKED_TEST_CASE)) {
                return p.getValue().getValue().getLinkedTestCase().getEnabledProperty();
            } else {
                return new SimpleBooleanProperty();
            }
        });
    }
}
