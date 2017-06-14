package sde.application.data.model;

import sde.application.data.NodeColour;
import sde.application.data.SavableAttribute;
import sde.application.data.User;
import sde.application.data.model.links.*;
import sde.application.error.Error;
import sde.application.gui.Program;
import sde.application.net.proxy.RecordedHeader;
import sde.application.net.proxy.RecordedProxy;
import sde.application.net.proxy.RecordedRequest;
import sde.application.node.design.DrawableNode;
import sde.application.node.implementations.*;
import sde.application.node.objects.*;
import sde.application.node.objects.datatable.DataTableRow;
import sde.application.node.objects.datatable.DataTableValue;
import sde.application.test.TestCommand;
import sde.application.test.TestCommandScreenshot;
import sde.application.test.TestLogMessage;
import sde.application.test.core.TestCase;
import sde.application.test.core.TestSet;
import sde.application.test.core.TestSetBatch;
import sde.application.utils.CustomObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DatabaseLink {
    private static HashMap<Class, Class> linkClasses = new HashMap<>();

    // Need to find an alternative to doing this
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
        linkClasses.put(DrawableNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(SavableAttribute.class, SavableAttributeDatabaseLink.class);
        linkClasses.put(CustomObject.class, CustomObjectDatabaseLink.class);
        linkClasses.put(TestCommand.class, TestCommandDatabaseLink.class);
        linkClasses.put(Test.class, TestDatabaseLink.class);
        linkClasses.put(LinkedTestCase.class, LinkedTestCaseDatabaseLink.class);
        linkClasses.put(TestCase.class, TestCaseDatabaseLink.class);
        linkClasses.put(TestSet.class, TestSetDatabaseLink.class);
        linkClasses.put(TestSetBatch.class, TestSetBatchDatabaseLink.class);
        linkClasses.put(TestLogMessage.class, TestLogMessageDatabaseLink.class);
        linkClasses.put(TestCommandScreenshot.class, TestCommandScreenshotDatabaseLink.class);

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
        linkClasses.put(LogTrackerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(RequestTrackerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(SwitchNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TestCaseNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TestNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TestManagerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TimerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(TriggerNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(WindowsNode.class, DrawableNodeDatabaseLink.class);
        linkClasses.put(ImportNode.class, DrawableNodeDatabaseLink.class);
    }

    private String tableName = "";
    private List<ModelColumn> modelColumns = new ArrayList<>();
    private List<ModelChild> modelChildLinks = new ArrayList<>();
    private List<DeleteColumn> onDeleteColumns = new ArrayList<>();
    private Class linkClass;

    public DatabaseLink(String tableName, Class linkClass) {
        this.tableName = tableName;
        this.linkClass = linkClass;
    }

    public static DatabaseLink getNewInstance(Class clazz) {
        try {
            return (DatabaseLink) clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            Error.DATABASE_SAVE_CLASS_INIT.record().additionalInformation("Class " + clazz).create(ex);
        } catch (NullPointerException ex) {
            Error.DATABASE_SAVE_CLASS_INIT.record()
                    .additionalInformation("New instance class was null")
                    .additionalInformation("Class " + clazz).create(ex);
        }

        return null;
    }

    public static DatabaseLink getNewInstanceFromBaseClass(Class clazz) {
        try {
            return getNewInstance(linkClasses.get(clazz));
        } catch (NullPointerException ex) {
            Error.DATABASE_SAVE_CLASS_INIT.record()
                    .additionalInformation("Class/DBLink likely not defined")
                    .additionalInformation("Class " + clazz).create(ex);
        }

        return null;
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

    public void child(Class databaseLinkClass, String referenceID) {
        modelChildLinks.add(new ModelChild(databaseLinkClass, referenceID));
    }

    public List<ModelChild> getModelChildLinks() {
        return modelChildLinks;
    }

    public void linkBlob(String databaseColumn, Method objectSaveMethod, Method objectLoadMethod) {
        modelColumns.add(new ModelColumn(databaseColumn, objectSaveMethod, objectLoadMethod, ModelColumn.BLOB_COLUMN));
    }

    public Class getLinkClass() {
        return linkClass;
    }

    public Method method(String methodName, Class<?>... parameterTypes) {
        try {
            return getLinkClass().getMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException ex) {
            Error.DATA_LINK_METHOD_NOT_FOUND.record()
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
            Error.DATA_LINK_METHOD_NOT_FOUND.record().create(ex);
        }

        return null;
    }

    public void onDelete(String columnName, Class clazz) {
        onDeleteColumns.add(new DeleteColumn(columnName, clazz));
    }
}
