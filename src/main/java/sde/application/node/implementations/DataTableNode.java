package sde.application.node.implementations;

import sde.application.data.SavableAttribute;
import sde.application.data.model.dao.DataTableRowDAO;
import sde.application.gui.Controller;
import sde.application.gui.UI;
import sde.application.node.design.DrawableNode;
import sde.application.utils.NodeRunParams;
import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import sde.application.node.objects.datatable.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DataTableNode extends DrawableNode {
    private static Logger log = Logger.getLogger(DataTableNode.class);
    private List<DataTableColumn> dataTableColumns = new ArrayList<>();
    private SpreadsheetView spreadsheetView = new SpreadsheetView();
    private DataTableWithHeader dataTableGrid;

    private TabPane dataTableTabPane = null;
    private Tab dataViewTab;
    private Tab renameColumnsTab;

    private TextField renameTextField;
    private ListView<DataTableNodeRenameListItem> columnListView;
    private Button orderUpButton;
    private Button orderDownButton;

    private VBox rightSection;
    private HBox detailsHBox;

    private String selectedValue = "";

    private ObservableList<DataTableRow> dataTableRows = null;

    // This will make a copy of the node passed to it
    public DataTableNode(DataTableNode dataTableNode) {
        this.setX(dataTableNode.getX());
        this.setY(dataTableNode.getY());
        this.setWidth(dataTableNode.getWidth());
        this.setHeight(dataTableNode.getHeight());
        this.setColor(dataTableNode.getColor());
        this.setScale(dataTableNode.getScale());
        this.setContainedText(dataTableNode.getContainedText());
        //this.setProgramUuid(dataTableNode.getProgramUuid());
        this.setNextNodeToRun(dataTableNode.getNextNodeToRun());
    }

    public DataTableNode() {
        super();
    }

    public static List<DataTableNodeRenameListItem> findColumnNames(List<DataTableRow> dataTableRowList) {
        List<DataTableNodeRenameListItem> columnNames = new ArrayList<>();
        List<DataTableValue> dataTableValues = new ArrayList<>();
        for (DataTableRow dataTableRow : dataTableRowList) {
            dataTableValues.addAll(dataTableRow.getDataTableValuesCollection().getOrderedValues());
        }

        Collections.sort(dataTableValues); // Should order by order by in DataTableValue

        for (DataTableValue dataTableValue : dataTableValues) {
            Boolean alreadyContains = false;
            for (DataTableNodeRenameListItem dataTableNodeRenameListItem : columnNames) {
                if (dataTableNodeRenameListItem.equals(dataTableValue.getDataKey())) {
                    alreadyContains = true;
                }
            }

            if (!alreadyContains) {
                columnNames.add(dataTableValue.getAsListItem());
            }
        }

        return columnNames;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.addAll(super.getDataToSave());
        getDataTableRows().forEach(DataTableRow::save);

        return savableAttributes;
    }

    public void saveObjects() {
        getDataTableRows().forEach(DataTableRow::save);
    }

    public void addAllDataTableRow(List<DataTableRow> dataTableRowList) {
        getDataTableRows().addAll(dataTableRowList);
        buildDataGrid();
    }

    public void addDataTableRow(DataTableRow dataTableRow) {
        List<DataTableRow> dataTableRows1 = new ArrayList<>();
        dataTableRows1.add(dataTableRow);
        addAllDataTableRow(dataTableRows1);
    }

    private void buildDataGrid() {
        dataTableGrid = new DataTableWithHeader(getDataTableRows());
        spreadsheetView.setGrid(dataTableGrid.getGrid());
        if (columnListView != null) {
            columnListView.getItems().clear();
            columnListView.getItems().addAll(findColumnNames(getDataTableRows()));
        }
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {

    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();
        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = controller.getContentAnchorPaneOfTab(tab);

        dataTableTabPane = new TabPane();

        // Create View Data
        dataViewTab = new Tab("View Data");
        dataViewTab.setClosable(false);
        createViewDataTab();

        // Create Rename columns
        renameColumnsTab = new Tab("Rename/Edit Columns");
        renameColumnsTab.setClosable(false);
        createRenameColumnTab();

        // Add created tabs to main frame
        dataTableTabPane.getTabs().addAll(dataViewTab, renameColumnsTab);

        UI.setAnchorMargins(dataTableTabPane, 50.0, 0.0, 0.0, 0.0);

        anchorPane.getChildren().addAll(dataTableTabPane);
        return tab;
    }

    public void createViewDataTab() {
        if (dataViewTab != null) {
            AnchorPane dataViewAnchorPane = new AnchorPane();

            if (dataTableColumns.size() == 0) {
                dataTableColumns.add(new DataTableColumn("New Column"));
            }

            UI.setAnchorMargins(dataViewAnchorPane, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(spreadsheetView, 0.0, 0.0, 0.0, 0.0);

            dataViewAnchorPane.getChildren().add(spreadsheetView);

            dataTableGrid = new DataTableWithHeader(getDataTableRows());
            spreadsheetView.setGrid(dataTableGrid.getGrid());

            MenuItem addNewRow = new MenuItem("Add New Row");
            addNewRow.setOnAction(event -> {
                DataTableRow dataTableRow = DataTableRow.create(DataTableRow.class);
                dataTableRow.setParent(this);
                dataTableRow.save();
                addDataTableRow(dataTableRow);

                for (DataTableNodeRenameListItem columnName : findColumnNames(getDataTableRows())) {
                    DataTableValue dataTableValue = DataTableValue.create(DataTableValue.class);
                    dataTableValue.setDataKey(columnName.getValue());
                    dataTableValue.setDataValue("");
                    dataTableValue.setParentRow(dataTableRow);
                    dataTableValue.save();
                    dataTableRow.addDataTableValue(dataTableValue);
                }

                buildDataGrid();
            });

            MenuItem addNewColumn = new MenuItem("Add New Column");
            addNewColumn.setOnAction(event -> {
                if (getDataTableRows().size() == 0) {
                    DataTableRow dataTableRow = DataTableRow.create(DataTableRow.class);
                    dataTableRow.setParent(this);
                    dataTableRow.save();
                    addDataTableRow(dataTableRow);
                }

                for (DataTableRow dataTableRow : getDataTableRows()) {
                    DataTableValue dataTableValue = DataTableValue.create(DataTableValue.class);
                    dataTableValue.setDataKey("New Column");
                    dataTableValue.setDataValue("");
                    dataTableValue.setParentRow(dataTableRow);
                    dataTableValue.save();
                    dataTableRow.addDataTableValue(dataTableValue);
                }

                buildDataGrid();
            });

            MenuItem deleteColumn = new MenuItem("Delete Column(s)");
            deleteColumn.setOnAction(event -> {
                for (TablePosition tablePosition : spreadsheetView.getSelectionModel().getSelectedCells()) {
                    for (DataTableRow dataTableRow : getDataTableRows()) {
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
                    dataTableRowsToDelete.add(getDataTableRows().get(rowNumber));
                }

                for (DataTableRow dataTableRow : dataTableRowsToDelete) {
                    getDataTableRows().remove(dataTableRow);
                    dataTableRow.delete();
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

            UI.setAnchorMargins(renameColumnsAnchorPane, 0.0, 0.0, 0.0, 0.0);
            UI.setAnchorMargins(detailsHBox, 10.0, 10.0, 10.0, 10.0);

            List<DataTableNodeRenameListItem> columnNames = findColumnNames(getDataTableRows());

            rightSection = new VBox(5);
            detailsHBox = new HBox(5);

            Label nameLabel = new Label("Name:");
            nameLabel.setPadding(new Insets(4, 0, 0, 0));

            renameTextField = new TextField();
            renameTextField.setOnAction(event -> {
                TextField eventTextField = (TextField) event.getSource();
                if (!eventTextField.getText().isEmpty()) {
                    for (DataTableRow dataTableRow : getDataTableRows()) {
                        String newValue = dataTableRow.getData(eventTextField.getId());

                        if (newValue != null) {
                            DataTableValue dataTableValue = DataTableValue.create(DataTableValue.class);
                            dataTableValue.setDataKey(eventTextField.getText());
                            dataTableValue.setDataValue(newValue);
                            dataTableValue.setParentRow(dataTableRow);
                            dataTableValue.save();

                            dataTableRow.addDataTableValue(dataTableValue);
                            dataTableRow.removeDataTableValue(eventTextField.getId());

                            selectedValue = newValue;
                        }
                    }

                    eventTextField.setId(eventTextField.getText());
                }

                buildDataGrid();
            });
            renameTextField.setText("");
            renameTextField.setId("");
            renameTextField.setDisable(true);

            orderUpButton = new Button();
            orderUpButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.ARROW_UP).build());
            orderUpButton.setOnAction(event -> orderChanged(true));

            orderDownButton = new Button();
            orderDownButton.setGraphic(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.ARROW_DOWN).build());
            orderDownButton.setOnAction(event -> orderChanged(false));

            detailsHBox.getChildren().add(nameLabel);
            detailsHBox.getChildren().add(renameTextField);

            rightSection.getChildren().add(detailsHBox);
            rightSection.getChildren().add(orderUpButton);
            rightSection.getChildren().add(orderDownButton);

            columnListView = new ListView<>();
            columnListView.getItems().addAll(columnNames);
            columnListView.getSelectionModel().selectedItemProperty().addListener(
                    (ov, oldString, newString) -> {
                        if (newString != null && !newString.getValue().isEmpty()) {
                            renameTextField.setText(newString.getValue());
                            renameTextField.setId(newString.getValue());
                            renameTextField.setDisable(false);
                            selectedValue = newString.getValue();
                        }
                    });

            HBox hbox = new HBox(5);
            UI.setAnchorMargins(hbox, 10.0, 10.0, 10.0, 10.0);

            hbox.getChildren().add(columnListView);
            hbox.getChildren().add(rightSection);

            renameColumnsAnchorPane.getChildren().add(hbox);

            renameColumnsTab.setContent(renameColumnsAnchorPane);
        }
    }

    private void orderChanged(Boolean up) {
        Integer newOrder = null;
        for (DataTableRow dataTableRow : getDataTableRows()) {
            if (selectedValue != null) {
                DataTableValue dataTableValue = dataTableRow.getDataTableValuesCollection().get(selectedValue);

                if (dataTableValue != null) {
                    if (newOrder == null) {
                        if (up) {
                            newOrder = dataTableValue.getOrder() - 1;
                        } else {
                            newOrder = dataTableValue.getOrder() + 1;
                        }
                    }

                    dataTableValue.setOrder(newOrder);
                    dataTableValue.save();
                }

                dataTableRow.getDataTableValuesCollection().sort();
            }
        }

        columnListView.getItems().clear();
        columnListView.getItems().addAll(findColumnNames(getDataTableRows()));

        buildDataGrid();

        if (selectedValue != null) {
            Integer selectedIndex = 0;
            Integer currentIndex = 0;
            for (DataTableNodeRenameListItem dataTableNodeRenameListItem : columnListView.getItems()) {
                if (selectedValue != null) {
                    if (dataTableNodeRenameListItem.equals(selectedValue)) {
                        selectedIndex = currentIndex;
                    }
                }
                currentIndex++;
            }
            columnListView.getSelectionModel().select(selectedIndex);
            columnListView.getFocusModel().focus(selectedIndex);
            columnListView.scrollTo(selectedIndex);
        }
    }

    public ObservableList<DataTableRow> getDataTableRows() {
        if (dataTableRows == null) {
            DataTableRowDAO dataTableRowDAO = new DataTableRowDAO();
            dataTableRows = FXCollections.observableArrayList();
            dataTableRows.addAll(dataTableRowDAO.getDataTableRowByNode(this));
        }
        return dataTableRows;
    }
}
