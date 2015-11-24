package application.data.model.dao;

import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.node.objects.datatable.DataTableRow;
import application.node.objects.datatable.DataTableValue;

import java.util.ArrayList;
import java.util.List;

public class DataTableValueDAO {
    public DataTableValueDAO() {

    }

    public List<DataTableValue> getDataTableValueByRow(DataTableRow dataTableRow) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from data_table_values where data_table_id = ?")
                .addParameter(dataTableRow.getUuidString()) // 1
                .execute();
        List<DataTableValue> dataTableValues = new ArrayList<>();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            DataTableValue dataTableValue = DataTableValue.load(DAO.UUIDFromString(resultRow.getString("uuid")), DataTableValue.class);
            dataTableValues.add(dataTableValue);
        }

        return dataTableValues;
    }
}
