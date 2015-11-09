package application.data.model;

import application.data.NodeColour;
import application.data.User;
import application.data.model.links.*;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedProxy;
import application.net.proxy.RecordedRequest;
import application.node.objects.Input;
import application.node.objects.Trigger;
import application.node.objects.datatable.DataTableRow;
import application.node.objects.datatable.DataTableValue;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseLink {
    private String tableName = "";
    private List<ModelColumn> modelColumns = new ArrayList<>();

    private static HashMap<Class, Class> linkClasses = new HashMap<>();

    // need to find an alternative to doing this
    static {
        linkClasses.put(Input.class, InputDatabaseLink.class);
        linkClasses.put(NodeColour.class, NodeColourDatabaseLink.class);
        linkClasses.put(Trigger.class, TriggerDatabaseLink.class);
        linkClasses.put(User.class, UserDatabaseLink.class);
        linkClasses.put(DataTableValue.class, DataTableValueDatabaseLink.class);
        linkClasses.put(DataTableRow.class, DataTableRowDatabaseLink.class);
        linkClasses.put(RecordedRequest.class, RecordedRequestDatabaseLink.class);
        linkClasses.put(RecordedProxy.class, RecordedProxyDatabaseLink.class);
        linkClasses.put(RecordedHeader.class, RecordedHeaderDatabaseLink.class);
    }

    public DatabaseLink(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ModelColumn> getModelColumns() {
        return modelColumns;
    }

    public void link(String databaseColumn, Method objectMethod) {
        modelColumns.add(new ModelColumn(databaseColumn, objectMethod, ModelColumn.STANDARD_COLUMN));
    }

    public void linkBlob(String databaseColumn, Method objectMethod) {
        modelColumns.add(new ModelColumn(databaseColumn, objectMethod, ModelColumn.BLOB_COLUMN));
    }

    public static Class getLinkClass(Class clazz) {
        return linkClasses.get(clazz);
    }
}
