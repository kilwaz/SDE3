package sde.application.data.model.dao;

import sde.application.data.SavableAttribute;
import sde.application.data.SelectQuery;
import sde.application.data.SelectResult;
import sde.application.data.SelectResultRow;
import sde.application.data.model.links.DrawableNodeDatabaseLink;
import sde.application.error.Error;
import sde.application.gui.Program;
import sde.application.node.design.DrawableNode;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DrawableNodeDAO {
    private static Logger log = Logger.getLogger(DrawableNodeDAO.class);

    public void DrawableNodeDAO() {

    }

    public DrawableNode getDrawableNodeUnknownClassFromUuid(UUID uuid) {
        DrawableNodeDatabaseLink drawableNodeDatabaseLink = new DrawableNodeDatabaseLink();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid, node_type from " + drawableNodeDatabaseLink.getTableName() + " where uuid = ?")
                .addParameter(uuid.toString())
                .execute();

        DrawableNode drawableNode = null;

        for (SelectResultRow resultRow : selectResult.getResults()) {
            try {
                Class<DrawableNode> clazz = (Class<DrawableNode>) Class.forName("sde.application.node.implementations." + resultRow.getString("node_type"));
                drawableNode = loadNodeWithVariables(DAO.UUIDFromString(resultRow.getString("uuid")), clazz);
            } catch (ClassNotFoundException ex) {
                Error.NODE_CLASS_NOT_FOUND.record().additionalInformation("Class: " + resultRow.getString("node_type")).create(ex);
            }
        }

        return drawableNode;
    }

    public List<DrawableNode> getNodes(Program program) {
        List<DrawableNode> drawableNodes = new ArrayList<>();

        DrawableNodeDatabaseLink drawableNodeDatabaseLink = new DrawableNodeDatabaseLink();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid, node_type from " + drawableNodeDatabaseLink.getTableName() + " where program_id = ?")
                .addParameter(program.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            try {
                Class<DrawableNode> clazz = (Class<DrawableNode>) Class.forName("sde.application.node.implementations." + resultRow.getString("node_type"));
                drawableNodes.add(loadNodeWithVariables(DAO.UUIDFromString(resultRow.getString("uuid")), clazz));
            } catch (ClassNotFoundException ex) {
                Error.NODE_CLASS_NOT_FOUND.record().additionalInformation("Class: " + resultRow.getString("node_type")).create(ex);
            }
        }

        return drawableNodes;
    }

    public DrawableNode loadNodeWithVariables(UUID uuid, Class<DrawableNode> clazz) {
        DrawableNode drawableNode = DrawableNode.load(uuid, clazz);

        // We set this to stop the object saving updates from the initial load
        drawableNode.setIsInitialising(true);

        SavableAttributeDAO savableAttributeDAO = new SavableAttributeDAO();
        List<SavableAttribute> savableAttributes = savableAttributeDAO.getAttributes(drawableNode);
        for (SavableAttribute savableAttribute : savableAttributes) {
            Method method = null;
            Object variable = "";
            try {
                if ("java.lang.Double".equals(savableAttribute.getClassName())) {
                    Double doubleValue = 0d;
                    Object variableObject = savableAttribute.getVariable();
                    if (variableObject instanceof Double) {
                        doubleValue = (Double) variableObject;
                    } else if (variableObject instanceof byte[]) {
                        doubleValue = Double.parseDouble(new String((byte[]) variableObject));
                    }

                    method = drawableNode.getClass().getMethod("set" + savableAttribute.getVariableName(), Class.forName(savableAttribute.getClassName()));
                    method.invoke(drawableNode, doubleValue);
                } else if ("java.lang.String".equals(savableAttribute.getClassName())) {
                    String stringValue = "";
                    Object variableObject = savableAttribute.getVariable();
                    if (variableObject instanceof String) {
                        stringValue = (String) variableObject;
                    } else if (variableObject instanceof byte[]) {
                        stringValue = new String((byte[]) variableObject);
                    }

                    method = drawableNode.getClass().getMethod("set" + savableAttribute.getVariableName(), Class.forName(savableAttribute.getClassName()));
                    method.invoke(drawableNode, stringValue);
                } else if ("java.lang.Integer".equals(savableAttribute.getClassName())) {
                    Integer integerValue = 0;
                    Object variableObject = savableAttribute.getVariable();
                    if (variableObject instanceof Integer) {
                        integerValue = (Integer) variableObject;
                    } else if (variableObject instanceof byte[]) {
                        integerValue = Integer.parseInt(new String((byte[]) variableObject));
                    }

                    method = drawableNode.getClass().getMethod("set" + savableAttribute.getVariableName(), Class.forName(savableAttribute.getClassName()));
                    method.invoke(drawableNode, integerValue);
                } else if ("java.lang.Boolean".equals(savableAttribute.getClassName())) {
                    Boolean booleanValue = false;
                    Object variableObject = savableAttribute.getVariable();
                    if (variableObject instanceof Boolean) {
                        booleanValue = (Boolean) variableObject;
                    } else if (variableObject instanceof byte[]) {
                        String booleanStr = new String((byte[]) variableObject);
                        // Here 1 = true and 0 = false
                        booleanValue = "1".equals(booleanStr);
                    }

                    method = drawableNode.getClass().getMethod("set" + savableAttribute.getVariableName(), Class.forName(savableAttribute.getClassName()));
                    method.invoke(drawableNode, booleanValue);
                }
            } catch (Exception ex) {
                String additionalInfo = "";
                if (method != null) {
                    additionalInfo = "Method: " + method.getName();
                }
                Error.LOADING_PROGRAM.record().additionalInformation(additionalInfo).additionalInformation("Value: " + variable.toString()).create(ex);
            }
        }

        drawableNode.setIsInitialising(false);

        return drawableNode;
    }
}
