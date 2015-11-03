package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.datatable.*;
import application.utils.NodeRunParams;
import application.utils.SDEUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

public class DataTableNode extends DrawableNode {
    private List<DataTableColumn> dataTableColumns = new ArrayList<>();

    private static Logger log = Logger.getLogger(DataTableNode.class);

    private SpreadsheetView spreadsheetView = new SpreadsheetView();
    private DataTableWithHeader dataTableGrid;

    ObservableList<DataTableRow> dataTableRows = FXCollections.observableArrayList();

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
        dataTableRows.forEach(DataTableRow::save);

        return savableAttributes;
    }

    public void loadObjects() {

        DataBank.loadDataTableRows(this);
    }

    public void saveObjects() {
        dataTableRows.forEach(DataTableRow::save);
    }

    public void addAllDataTableRow(List<DataTableRow> dataTableRowList) {
        dataTableRows.addAll(dataTableRowList);
        buildDataGrid();
    }

    public void addDataTableRow(DataTableRow dataTableRow) {
        List<DataTableRow> dataTableRows1 = new ArrayList<>();
        dataTableRows1.add(dataTableRow);
        addAllDataTableRow(dataTableRows1);
    }

    private void buildDataGrid() {
        dataTableGrid = new DataTableWithHeader(dataTableRows);
        spreadsheetView.setGrid(dataTableGrid.getGrid());
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

        if (dataTableColumns.size() == 0) {
            dataTableColumns.add(new DataTableColumn("New Column"));
        }

        AnchorPane.setBottomAnchor(dataViewAnchorPane, 0.0);
        AnchorPane.setLeftAnchor(dataViewAnchorPane, 0.0);
        AnchorPane.setRightAnchor(dataViewAnchorPane, 0.0);
        AnchorPane.setTopAnchor(dataViewAnchorPane, 0.0);
        dataViewTab.setContent(dataViewAnchorPane);

        AnchorPane.setBottomAnchor(spreadsheetView, 0.0);
        AnchorPane.setLeftAnchor(spreadsheetView, 0.0);
        AnchorPane.setRightAnchor(spreadsheetView, 0.0);
        AnchorPane.setTopAnchor(spreadsheetView, 0.0);

        dataViewAnchorPane.getChildren().add(spreadsheetView);

        dataTableGrid = new DataTableWithHeader(dataTableRows);
        spreadsheetView.setGrid(dataTableGrid.getGrid());

        MenuItem addNewRow = new MenuItem("Add New Row");
        addNewRow.setOnAction(event -> {
            DataBank.createNewDataTableRow(this);

            spreadsheetView.getSelectionModel().getFocusedCell();
        });

        MenuItem deleteRow = new MenuItem("Delete Row(s)");
        deleteRow.setOnAction(event -> {
            List<Integer> rowsToDelete = new ArrayList<>();
            List<DataTableRow> dataTableRowsToDelete = new ArrayList<>();

            for (TablePosition tablePosition : spreadsheetView.getSelectionModel().getSelectedCells()) {
                if (!rowsToDelete.contains(tablePosition.getRow())) {
                    rowsToDelete.add(tablePosition.getRow());
                }
            }

            for (Integer rowNumber : rowsToDelete) {
                dataTableRowsToDelete.add(dataTableRows.get(rowNumber));
            }

            for (DataTableRow dataTableRow : dataTableRowsToDelete) {
                dataTableRows.remove(dataTableRow);
                DataBank.deleteDataTableRow(dataTableRow);
            }

            buildDataGrid();
        });

        spreadsheetView.getContextMenu().getItems().add(addNewRow);
        spreadsheetView.getContextMenu().getItems().add(deleteRow);

        dataTableTabPane.getTabs().add(dataViewTab);

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

//    private void addColumnToTable(DataTableColumn dataTableColumn) {
//        TableColumn newTableColumn = new TableColumn(dataTableColumn.getTitle());
//        newTableColumn.setMinWidth(30);
//        newTableColumn.setCellValueFactory(new DataTablePropertyValueFactory<DataTableRow, String>(dataTableColumn));
//        newTableColumn.setCellFactory(column -> new DataTableCell(dataTableColumn.getTitle(), this));
//        newTableColumn.setEditable(true);
//
//        DataTableNode thisNode = this;
//
//        // Create content menu for renaming the columns
//        ContextMenu contextMenu = new ContextMenu();
//        MenuItem renameColumn = new MenuItem("Rename Column");
//        renameColumn.setOnAction(event -> {
//            String oldName = newTableColumn.getText();
//            newTableColumn.setText(null);
//            newTableColumn.setPrefWidth(newTableColumn.getWidth());
//            TextField colHeaderTextField = new TextField(oldName);
//
//            colHeaderTextField.setOnKeyPressed(event2 -> {
//                if (event2.getCode() == KeyCode.ENTER) {
//                    renameColumn(newTableColumn, colHeaderTextField.getText(), oldName, dataTableColumn);
//                }
//            });
//
//            newTableColumn.setGraphic(colHeaderTextField);
//        });
//        contextMenu.getItems().add(renameColumn);
//
//        newTableColumn.setContextMenu(contextMenu);
//
//        dataTableView.getColumns().add(newTableColumn);
//    }

    private void renameColumn(TableColumn newTableColumn, String newName, String oldName, DataTableColumn dataTableColumn) {
        // Go through each row and replace swap the key against the old value for the new column name
        for (DataTableRow dataTableRow : getDataTableRows()) {
            String oldValue = dataTableRow.getData(oldName);
            if (oldValue != null) {
                dataTableRow.updateDataTableValue(newName, oldValue);
                dataTableRow.removeDataTableValue(oldName);
            }
        }

        dataTableColumn.setTitle(newName);

        newTableColumn.setGraphic(null);
        newTableColumn.setText(newName);
        newTableColumn.setCellValueFactory(new DataTablePropertyValueFactory<DataTableRow, String>(dataTableColumn));
        newTableColumn.setCellFactory(column -> new DataTableCell(newName, this));
    }

//    public void recalculateColumnNames() {
//        dataTableColumns.clear();
//
//        // Find all the columns in the data that are being used at the moment
//        List<String> allColumns = new ArrayList<>();
//        for (DataTableRow dataTableRow : dataTableView.getItems()) {
//            dataTableRow.getDataTableValues().keySet().stream().filter(key -> !allColumns.contains(key)).forEach(allColumns::add);
//        }
//
//        // Add a new column for each name identified
//        dataTableColumns.addAll(allColumns.stream().map(DataTableColumn::new).collect(Collectors.toList()));
//    }

    public ObservableList<DataTableRow> getDataTableRows() {
        return dataTableRows;
    }
}
