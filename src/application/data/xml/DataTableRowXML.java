package application.data.xml;

import application.node.objects.datatable.DataTableRow;
import application.node.objects.datatable.DataTableValue;
import application.utils.SDEUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DataTableRowXML implements XML {
    DataTableRow dataTableRow;

    public DataTableRowXML(DataTableRow dataTableRow) {
        this.dataTableRow = dataTableRow;
    }

    @Override
    public Document getXMLRepresentation() {
        return null;
    }

    @Override
    public Element getXMLRepresentation(Document document) {
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

        return dataTableRowsElement;
    }
}
