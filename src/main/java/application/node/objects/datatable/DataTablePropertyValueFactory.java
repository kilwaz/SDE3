package application.node.objects.datatable;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class DataTablePropertyValueFactory<S, T> implements Callback<TableColumn.CellDataFeatures<S, T>, ObservableValue<String>> {
    private DataTableColumn dataTableColumn;

    public DataTablePropertyValueFactory(DataTableColumn dataTableColumn) {
        this.dataTableColumn = dataTableColumn;
    }

    @Override
    public ObservableValue<String> call(TableColumn.CellDataFeatures<S, T> param) {
        if (param.getValue() instanceof DataTableRow) {
            DataTableRow dataTableRow = (DataTableRow) param.getValue();
            dataTableRow.getData(dataTableColumn.getTitle());
            return new ReadOnlyObjectWrapper<>(dataTableRow.getData(dataTableColumn.getTitle()));
        }
        return null;
    }
}
