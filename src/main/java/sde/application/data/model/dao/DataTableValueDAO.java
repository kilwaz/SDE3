package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.node.objects.datatable.DataTableRow;
import sde.application.node.objects.datatable.DataTableValue;

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
