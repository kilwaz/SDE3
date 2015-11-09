package application.node.implementations;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.node.objects.datatable.DataTableColumn;
import application.node.objects.datatable.DataTableRow;
import application.node.objects.datatable.DataTableValue;
import application.node.objects.datatable.DataTableWithHeader;
import application.utils.NodeRunParams;
import application.utils.SDEUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DataTableNode extends DrawableNode {
    private List<DataTableColumn> dataTableColumns = new ArrayList<>();

    private static Logger log = Logger.getLogger(DataTableNode.class);

    private SpreadsheetView spreadsheetView = new SpreadsheetView();
    private DataTableWithHeader dataTableGrid;

    private Tab dataViewTab;
    private Tab renameColumnsTab;

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
        createRenameColumnTab();
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        TabPane dataTableTabPane = new TabPane();

        // Create View Data
        dataViewTab = new Tab("View Data");
        dataViewTab.setClosable(false);
        createViewDataTab();

        // Create Rename columns
        renameColumnsTab = new Tab("Rename Columns");
        renameColumnsTab.setClosable(false);
        createRenameColumnTab();

        // Add created tabs to main frame

        dataTableTabPane.getTabs().addAll(dataViewTab, renameColumnsTab);

        AnchorPane.setBottomAnchor(dataTableTabPane, 0.0);
        AnchorPane.setLeftAnchor(dataTableTabPane, 0.0);
        AnchorPane.setRightAnchor(dataTableTabPane, 0.0);
        AnchorPane.setTopAnchor(dataTableTabPane, 50.0);

        anchorPane.getChildren().addAll(dataTableTabPane);
        return tab;
    }

    public void createViewDataTab() {
        if (dataViewTab != null) {
            AnchorPane dataViewAnchorPane = new AnchorPane();

            if (dataTableColumns.size() == 0) {
                dataTableColumns.add(new DataTableColumn("New Column"));
            }

            AnchorPane.setBottomAnchor(dataViewAnchorPane, 0.0);
            AnchorPane.setLeftAnchor(dataViewAnchorPane, 0.0);
            AnchorPane.setRightAnchor(dataViewAnchorPane, 0.0);
            AnchorPane.setTopAnchor(dataViewAnchorPane, 0.0);

            AnchorPane.setBottomAnchor(spreadsheetView, 0.0);
            AnchorPane.setLeftAnchor(spreadsheetView, 0.0);
            AnchorPane.setRightAnchor(spreadsheetView, 0.0);
            AnchorPane.setTopAnchor(spreadsheetView, 0.0);

            dataViewAnchorPane.getChildren().add(spreadsheetView);

            dataTableGrid = new DataTableWithHeader(dataTableRows);
            spreadsheetView.setGrid(dataTableGrid.getGrid());

            MenuItem addNewRow = new MenuItem("Add New Row");
            addNewRow.setOnAction(event -> DataBank.createNewDataTableRow(this));

            MenuItem addNewColumn = new MenuItem("Add New Column");
            addNewColumn.setOnAction(event -> {
                if (dataTableRows.size() == 0) {
                    dataTableRows.add(DataBank.createNewDataTableRow(this));
                }

                for (DataTableRow dataTableRow : dataTableRows) {
                    dataTableRow.addDataTableValue(DataBank.createNewDataTableValue(dataTableRow, "New Column", ""));
                }

                buildDataGrid();
            });

            MenuItem deleteColumn = new MenuItem("Delete Column(s)");
            deleteColumn.setOnAction(event -> {
                for (TablePosition tablePosition : spreadsheetView.getSelectionModel().getSelectedCells()) {
                    for (DataTableRow dataTableRow : dataTableRows) {
                        dataTableRow.removeDataTableValue(tablePosition.getTableColumn().getText());
                    }
                }
                buildDataGrid();
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
            spreadsheetView.getContextMenu().getItems().add(addNewColumn);
            spreadsheetView.getContextMenu().getItems().add(deleteRow);
            spreadsheetView.getContextMenu().getItems().add(deleteColumn);

            dataViewTab.setContent(dataViewAnchorPane);
        }
    }

    public void createRenameColumnTab() {
        if (renameColumnsTab != null) {
            AnchorPane renameColumnsAnchorPane = new AnchorPane();

            AnchorPane.setBottomAnchor(renameColumnsAnchorPane, 0.0);
            AnchorPane.setLeftAnchor(renameColumnsAnchorPane, 0.0);
            AnchorPane.setRightAnchor(renameColumnsAnchorPane, 0.0);
            AnchorPane.setTopAnchor(renameColumnsAnchorPane, 0.0);

            List<String> columnNames = findColumnNames(dataTableRows);

            VBox vbox = new VBox(5);
            AnchorPane.setBottomAnchor(vbox, 10.0);
            AnchorPane.setLeftAnchor(vbox, 10.0);
            AnchorPane.setRightAnchor(vbox, 10.0);
            AnchorPane.setTopAnchor(vbox, 10.0);

            for (String columnName : columnNames) {
                vbox.getChildren().add(createRenameColumnRow(columnName));
            }

            renameColumnsAnchorPane.getChildren().add(vbox);

            renameColumnsTab.setContent(renameColumnsAnchorPane);
        }
    }

    private HBox createRenameColumnRow(String columnName) {
        HBox hBox = new HBox(5);

        TextField textField = new TextField();
        textField.setOnAction(event -> {
            TextField eventTextField = (TextField) event.getSource();
            if (!eventTextField.getText().isEmpty()) {
                for (DataTableRow dataTableRow : dataTableRows) {
                    String oldValue = dataTableRow.getData(eventTextField.getId());

                    if (oldValue != null) {
                        dataTableRow.addDataTableValue(DataBank.createNewDataTableValue(dataTableRow, eventTextField.getText(), oldValue));
                        dataTableRow.removeDataTableValue(eventTextField.getId());

                    }
                }
                eventTextField.setId(eventTextField.getText());
            }

            buildDataGrid();
        });
        textField.setText(columnName);
        textField.setId(columnName);

        hBox.getChildren().add(textField);

        return hBox;
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

    public static List<String> findColumnNames(List<DataTableRow> dataTableRowList) {
        List<String> columnNames = new ArrayList<>();
        for (DataTableRow dataTableRow : dataTableRowList) {
            LinkedHashMap<String, DataTableValue> rowValues = dataTableRow.getDataTableValues();
            for (DataTableValue dataTableValue : rowValues.values()) {
                if (!columnNames.contains(dataTableValue.getDataKey())) {
                    columnNames.add(dataTableValue.getDataKey());
                }
            }
        }

        return columnNames;
    }

    public ObservableList<DataTableRow> getDataTableRows() {
        return dataTableRows;
    }
}
