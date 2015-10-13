package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.datatable.*;
import application.utils.NodeRunParams;
import application.utils.SDEUtils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class DataTableNode extends DrawableNode {
    private List<DataTableColumn> dataTableColumns = new ArrayList<>();
    private ObservableList<DataTableRow> dataTableRows = FXCollections.observableArrayList();

    // This will make a copy of the node passed to it
    public DataTableNode(DataTableNode dataTableNode) {
        this.setId(-1);
        this.setX(dataTableNode.getX());
        this.setY(dataTableNode.getY());
        this.setWidth(dataTableNode.getWidth());
        this.setHeight(dataTableNode.getHeight());
        this.setColor(dataTableNode.getColor());
        this.setScale(dataTableNode.getScale());
        this.setContainedText(dataTableNode.getContainedText());
        this.setProgramId(dataTableNode.getProgramId());
        this.setNextNodeToRun(dataTableNode.getNextNodeToRun());
    }

    public DataTableNode(Integer id, Integer programId) {
        super(id, programId);
        loadObjects();
    }

    public DataTableNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
        loadObjects();
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());
        dataTableRows.forEach(DataBank::saveDataTableRow);

        return savableAttributes;
    }

    public void loadObjects() {
        DataBank.loadDataTableRows(this);
    }

    public void saveObjects() {
        dataTableRows.forEach(DataBank::saveDataTableRow);
    }

    public void addDataTableRow(DataTableRow dataTableRow) {
        dataTableRows.add(dataTableRow);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        TabPane dataTableTabPane = new TabPane();

        // View Data
        Tab dataViewTab = new Tab("View Data");
        dataViewTab.setClosable(false);
        AnchorPane dataViewAnchorPane = new AnchorPane();

        TableView<DataTableRow> dataTableView = new TableView<>();

        if (dataTableRows.size() == 0) {
            dataTableRows.add(DataBank.createNewDataTableRow(this));
        }

        dataTableColumns.clear();
        dataTableColumns.add(new DataTableColumn("Hello"));
        dataTableColumns.add(new DataTableColumn("My"));
        dataTableColumns.add(new DataTableColumn("Friend"));
        dataTableColumns.add(new DataTableColumn("Dave"));

        dataTableView.setItems(dataTableRows);
        dataTableView.setEditable(true);

        for (DataTableColumn dataTableColumn : dataTableColumns) {
            TableColumn newTableColumn = new TableColumn(dataTableColumn.getTitle());
            newTableColumn.setMinWidth(30);
            newTableColumn.setCellValueFactory(new DataTablePropertyValueFactory<DataTableRow, String>(dataTableColumn));
            newTableColumn.setCellFactory(column -> new DataTableCell(dataTableColumn.getTitle(), this));
            newTableColumn.setEditable(true);
            dataTableView.getColumns().add(newTableColumn);
        }

        AnchorPane.setBottomAnchor(dataTableView, 0.0);
        AnchorPane.setLeftAnchor(dataTableView, 0.0);
        AnchorPane.setRightAnchor(dataTableView, 0.0);
        AnchorPane.setTopAnchor(dataTableView, 0.0);

        dataViewAnchorPane.getChildren().addAll(dataTableView);
        AnchorPane.setBottomAnchor(dataViewAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(dataViewAnchorPane, 0.0);
        AnchorPane.setRightAnchor(dataViewAnchorPane, 0.0);
        AnchorPane.setTopAnchor(dataViewAnchorPane, 0.0);
        dataViewTab.setContent(dataViewAnchorPane);

        // Right click context menu
        dataTableView.setRowFactory(tableView -> {
            TableRow<DataTableRow> row = new TableRow<>();
            ContextMenu contextMenu = new ContextMenu();
            MenuItem addNewRow = new MenuItem("Add New Row");

            addNewRow.setOnAction(event -> addDataTableRow(DataBank.createNewDataTableRow(this)));

            contextMenu.getItems().add(addNewRow);

            // Set context menu on row, but use a binding to make it only show for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(contextMenu)
            );
            return row;
        });

        // Edit Table
        Tab editView = new Tab("Edit Table");
        AnchorPane editViewAnchorPane = new AnchorPane();
        editView.setClosable(false);
        editView.setContent(editViewAnchorPane);

        dataTableTabPane.getTabs().add(dataViewTab);
        dataTableTabPane.getTabs().add(editView);

        AnchorPane.setBottomAnchor(dataTableTabPane, 0.0);
        AnchorPane.setLeftAnchor(dataTableTabPane, 0.0);
        AnchorPane.setRightAnchor(dataTableTabPane, 0.0);
        AnchorPane.setTopAnchor(dataTableTabPane, 50.0);

        anchorPane.getChildren().addAll(dataTableTabPane);

        return tab;
    }

    public Element getXMLRepresentation(Document document) {
        Element nodeElement = super.getXMLRepresentation(document);

        // Create a new element to save all inputs inside
        Element dataTableDataElement = document.createElement("DataTableData");

        for (DataTableRow dataTableRow : dataTableRows) {
            Element dataTableRowsElement = document.createElement("DataTableRows");

            for (DataTableValue dataTableValue : dataTableRow.getDataTableValues().values()) {
                Element dataTableValueElement = document.createElement("DataTableValue");

                Element dataKeyElement = document.createElement("DataKey");
                dataKeyElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(dataTableValue.getDataKey())));

                Element dataValueElement = document.createElement("DataValue");
                dataValueElement.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(dataTableValue.getDataValue())));

                dataTableValueElement.appendChild(dataKeyElement);
                dataTableValueElement.appendChild(dataValueElement);

                dataTableRowsElement.appendChild(dataTableValueElement);
            }

            dataTableDataElement.appendChild(dataTableRowsElement);
        }

        nodeElement.appendChild(dataTableDataElement);

        return nodeElement;
    }
}
