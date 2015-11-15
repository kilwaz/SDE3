package application.data.model.dao;

import application.data.SavableAttribute;
import application.data.SelectQuery;
import application.data.SelectResult;
import application.data.SelectResultRow;
import application.data.model.links.DrawableNodeDatabaseLink;
import application.error.Error;
import application.gui.Program;
import application.node.design.DrawableNode;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DrawableNodeDAO {
    public void DrawableNodeDAO() {

    }

    public List<DrawableNode> getNodes(Program program) {
        List<DrawableNode> drawableNodes = new ArrayList<>();

        DrawableNodeDatabaseLink drawableNodeDatabaseLink = new DrawableNodeDatabaseLink();

        SelectResult selectResult = (SelectResult) new SelectQuery("select uuid, node_type from " + drawableNodeDatabaseLink.getTableName() + " where program_id = ?")
                .addParameter(program.getUuidString())
                .execute();

        for (SelectResultRow resultRow : selectResult.getResults()) {
            try {
                Class<DrawableNode> clazz = (Class<DrawableNode>) Class.forName("application.node.implementations." + resultRow.getString("node_type"));
                DrawableNode drawableNode = DrawableNode.load(DAO.UUIDFromString(resultRow.getString("uuid")), clazz);

                // We set this to stop the object saving updates from the initial load
                drawableNode.setIsInitialising(true);

                SavableAttributeDAO savableAttributeDAO = new SavableAttributeDAO();
                List<SavableAttribute> savableAttributes = savableAttributeDAO.getAttributes(drawableNode);
                for (SavableAttribute savableAttribute : savableAttributes) {
                    Method method = null;
                    try {
                        if ("java.lang.Double".equals(savableAttribute.getClassName())) {
                            Double doubleValue = Double.parseDouble((String) savableAttribute.getVariable());

                            method = drawableNode.getClass().getMethod("set" + savableAttribute.getVariableName(), Class.forName(savableAttribute.getClassName()));
                            method.invoke(drawableNode, doubleValue);
                        } else if ("java.lang.String".equals(savableAttribute.getClassName())) {
                            String stringValue = (String) savableAttribute.getVariable();

                            method = drawableNode.getClass().getMethod("set" + savableAttribute.getVariableName(), Class.forName(savableAttribute.getClassName()));
                            method.invoke(drawableNode, stringValue);
                        } else if ("java.lang.Integer".equals(savableAttribute.getClassName())) {
                            Integer integerValue = Integer.parseInt((String) savableAttribute.getVariable());

                            method = drawableNode.getClass().getMethod("set" + savableAttribute.getVariableName(), Class.forName(savableAttribute.getClassName()));
                            method.invoke(drawableNode, integerValue);
                        }
                    } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException ex) {
                        String additionalInfo = "";
                        if (method != null) {
                            additionalInfo = "Method: " + method.getName();
                        }
                        Error.LOADING_PROGRAM.record().additionalInformation(additionalInfo).create(ex);
                    }
                }

                drawableNode.setIsInitialising(false);
                drawableNodes.add(drawableNode);
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }

        return drawableNodes;
    }
}
