package application.data.model;

import application.data.NodeColour;
import application.data.SavableAttribute;
import application.data.User;
import application.data.model.links.*;
import application.gui.Program;
import application.net.proxy.RecordedHeader;
import application.net.proxy.RecordedProxy;
import application.net.proxy.RecordedRequest;
import application.node.design.DrawableNode;
import application.node.implementations.*;
import application.node.objects.Input;
import application.node.objects.Switch;
import application.node.objects.Trigger;
import application.node.objects.datatable.DataTableRow;
import application.node.objects.datatable.DataTableValue;
import application.test.TestResult;
import application.test.TestStep;
import application.utils.CustomObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseLink {
    private String tableName = "";
    private List<ModelColumn> modelColumns = new ArrayList<>();
    private List<DeleteColumn> onDeleteColumns = new ArrayList<>();
    private Class linkClass;

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
        linkClasses.put(Program.class, ProgramDatabaseLink.class);
        linkClasses.put(Switch.class, SwitchDatabaseLink.class);
        linkClasses.put(TestStep.class, TestStepDatabaseLink.class);
        linkClasses.put(TestResult.class, TestResultDatabaseLink.class);
        linkClasses.put(DrawableNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(SavableAttribute.class, SavableAttributeDatabaseLink.class);
        linkClasses.put(CustomObject.class, CustomObjectDatabaseLink.class);

        // Drawable Node Classes - Better way to do this?
        linkClasses.put(BashNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(BatchNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(BulkTestNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(ChartNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(ConsoleNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(CopyNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(CustomObjectNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(DataBaseNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(DataTableNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(EmailNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(ExportNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(FileStoreNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(InputNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(LinuxNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(LogicNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(RequestTrackerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(SwitchNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TestNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TestResultNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TimerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TriggerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(WindowsNode.class, DrawableNodeDatabaseLink.class);
    }

    public DatabaseLink(String tableName, Class linkClass) {
        this.tableName = tableName;
        this.linkClass = linkClass;
    }

    public String getTableName() {
        return tableName;
    }

    public List<ModelColumn> getModelColumns() {
        return modelColumns;
    }

    public List<DeleteColumn> getOnDeleteColumns() {
        return onDeleteColumns;
    }

    public void link(String databaseColumn, Method objectSaveMethod, Method objectLoadMethod) {
        modelColumns.add(new ModelColumn(databaseColumn, objectSaveMethod, objectLoadMethod, ModelColumn.STANDARD_COLUMN));
    }

    public void linkBlob(String databaseColumn, Method objectSaveMethod, Method objectLoadMethod) {
        modelColumns.add(new ModelColumn(databaseColumn, objectSaveMethod, objectLoadMethod, ModelColumn.BLOB_COLUMN));
    }

    public Class getLinkClass() {
        return linkClass;
    }

    public static Class getLinkClass(Class clazz) {
        return linkClasses.get(clazz);
    }

    public Method method(String methodName, Class<?>... parameterTypes) {
        try {
            return getLinkClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ex) {
            application.error.Error.DATA_LINK_METHOD_NOT_FOUND.record()
                    .additionalInformation("Class: " + getLinkClass().getName())
                    .additionalInformation("MethodName: " + methodName)
                    .create(ex);
        }

        return null;
    }

    public Method externalMethod(String methodName, Class clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ex) {
            application.error.Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }

        return null;
    }

    public void onDelete(String columnName, Class clazz) {
        onDeleteColumns.add(new DeleteColumn(columnName, clazz));
    }
}
