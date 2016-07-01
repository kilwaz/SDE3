package application.gui.columns.testnode;

import application.node.objects.LinkedTestCase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

public class HierarchyTreeRow extends TreeTableRow<LinkedTestCaseTreeObject> {
    private static Logger log = Logger.getLogger(HierarchyTreeRow.class);
    private static TreeTableRow<LinkedTestCaseTreeObject> draggedTreeRow;

    public HierarchyTreeRow() {
//        setOnMouseClicked(event -> {
//            if (event.getButton() == MouseButton.PRIMARY) {
//                log.info("Clicked " + getTreeItem().getValue().getName());
//            }
//        });

        setOnDragOver(event -> {
            InnerShadow shadow;

            shadow = new InnerShadow();
            shadow.setOffsetX(1.0);
            shadow.setColor(Color.web("#666666"));
            shadow.setOffsetY(1.0);
            setEffect(shadow);

            event.acceptTransferModes(TransferMode.MOVE);
        });

        setOnDragDetected(event -> {
            ClipboardContent content = new ClipboardContent();
            content.putString("HierarchyTreeRow");

            Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
            dragboard.setContent(content);

            draggedTreeRow = this;
            event.consume();
        });

        setOnDragDropped(event -> {
            Boolean dropOK = false;

            if (draggedTreeRow != null) {
                TreeItem<LinkedTestCaseTreeObject> draggedTreeItem = draggedTreeRow.getTreeItem();
                TreeItem<LinkedTestCaseTreeObject> draggedOntoTreeItem = ((HierarchyTreeRow) event.getSource()).getTreeItem();

                draggedTreeItem.getParent().getChildren().remove(draggedTreeItem);
                draggedOntoTreeItem.getChildren().add(draggedTreeItem);
                draggedOntoTreeItem.setExpanded(true);
                LinkedTestCase parentLinkedTestCase = draggedTreeRow.getTreeItem().getValue().getLinkedTestCase();
                parentLinkedTestCase.setParentTestCase(getTreeItem().getValue().getLinkedTestCase());
                parentLinkedTestCase.save();
                dropOK = true;
                draggedTreeRow = null;
            }

            event.setDropCompleted(dropOK);
            event.consume();
        });

        setOnDragExited(event -> {
            setEffect(null);
        });
    }
}
