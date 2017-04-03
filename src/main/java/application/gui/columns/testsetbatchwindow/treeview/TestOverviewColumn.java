package application.gui.columns.testsetbatchwindow.treeview;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Node;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.image.ImageView;
import org.apache.log4j.Logger;

public class TestOverviewColumn extends TreeTableColumn<TestCaseTreeObject, Node> {
    private static Logger log = Logger.getLogger(TestOverviewColumn.class);

    public TestOverviewColumn() {
        setText("Overview");
        setPrefWidth(130);

        setCellValueFactory(p -> {
            ImageView imageView = new ImageView();
            imageView.setImage(p.getValue().getValue().getImageRepresentation());
            return new ReadOnlyObjectWrapper<>(imageView);
        });
    }
}
