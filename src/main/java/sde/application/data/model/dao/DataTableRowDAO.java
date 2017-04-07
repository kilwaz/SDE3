package sde.application.data.model.dao;

import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.node.implementations.DataTableNode;
import sde.application.node.objects.datatable.DataTableRow;

import java.util.ArrayList;
import java.util.List;

public class DataTableRowDAO {
    public DataTableRowDAO() {

    }

    public List<DataTableRow> getDataTableRowByNode(DataTableNode dataTableNode) {
        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid from data_table_rows where node_id = ?")
                .addParameter(dataTableNode.getUuidString()) // 1
                .execute();
        List<DataTableRow> dataTableRows = new ArrayList<>();
        for (SelectResultRow resultRow : selectResult.getResults()) {
            DataTableRow dataTableRow = DataTableRow.load(DAO.UUIDFromString(resultRow.getString("uuid")), DataTableRow.class);
            DataTableValueDAO dataTableValueDAO = new DataTableValueDAO();
            dataTableRow.addAllDataTableValue(dataTableValueDAO.getDataTableValueByRow(dataTableRow));

            dataTableRows.add(dataTableRow);
        }

        return dataTableRows;
    }
}
