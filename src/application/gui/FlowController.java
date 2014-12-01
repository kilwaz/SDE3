package application.gui;

import application.data.DataBank;
import application.node.*;
import javafx.scene.paint.Color;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class FlowController {
    private DrawableNode startNode;
    private DrawableNode selectedNode;
    private List<DrawableNode> nodes = new ArrayList<>();
    private List<NodeConnection> connections = new ArrayList<>();
    private List<NodeConnection> activeConnections = Collections.synchronizedList(new ArrayList<>());
    private String referenceID;
    private Program parentProgram;
    private ActiveRefreshTimer activeRefreshTimer = null;
    private Timer currentTimer;

    public FlowController(Program parentProgram) {
        this.parentProgram = parentProgram;
        startNode = new SourceNode(30.0, 30.0, "Start");
        startNode.setId(-1);
        referenceID = parentProgram.getId().toString();
    }

    public DrawableNode createNewNode(Integer id, Integer programId, String nodeType, Boolean isStartNode) {
        DrawableNode newNode = null;

        // Here we are searching for the class by name and calling the constructor manually to get our DrawableNode object
        try {
            Class<?> clazz = Class.forName("application.node." + nodeType);
            Constructor<?> ctor = clazz.getConstructor(Integer.class, Integer.class);
            newNode = (DrawableNode) ctor.newInstance(new Object[]{id, programId});
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (newNode != null) {
            nodes.add(newNode);
            if (isStartNode && newNode instanceof SourceNode) {
                startNode = newNode;
            }
        }

        return newNode;
    }

    public void addNode(DrawableNode drawableNode) {
        drawableNode.setProgramId(parentProgram.getId());
        nodes.add(drawableNode);
    }

    public void removeNode(DrawableNode drawableNode) {
        nodes.remove(drawableNode);
        ArrayList<NodeConnection> connectionsToRemove = new ArrayList<>();
        for (NodeConnection connection : connections) {
            if (connection.getConnectionStart() == drawableNode || connection.getConnectionEnd() == drawableNode) {
                connectionsToRemove.add(connection);
            }
        }

        for (NodeConnection connection : connectionsToRemove) {
            connections.remove(connection);
        }
    }

    public void addConnection(NodeConnection connection) {
        connections.add(connection);
    }

    public List<NodeConnection> getConnections() {
        return this.connections;
    }

    public List<DrawableNode> getNodes() {
        return this.nodes;
    }

    public DrawableNode getStartNode() {
        return startNode;
    }

    public void setStartNode(DrawableNode startNode) {
        this.startNode = startNode;
    }

    public String getReferenceID() {
        return this.referenceID;
    }

    public Boolean checkIfTreeIsCompiled() {
        Boolean result = true;

        for (DrawableNode node : nodes) {
            if (node instanceof SourceNode) {
                result = ((SourceNode) node).getSource().isCompiled();
                if (!result) {
                    break;
                }
            }
        }

        return result;
    }

    public Boolean compile() {
        for (DrawableNode node : nodes) {
            node.setColor(Color.DARKRED);
        }

        Controller.getInstance().updateCanvasControllerLater();

        for (DrawableNode node : nodes) {
            if (node instanceof SourceNode) {
                Boolean result = ((SourceNode) node).getSource().compile();
                if (result) {
                    node.setColor(Color.LIMEGREEN);
                } else {
                    node.setColor(Color.ORANGE);
                }
                Controller.getInstance().updateCanvasControllerLater();
            }
        }

        return true; // This should return what the actual compile method returns..
    }

    public void loadInstances() {
        for (DrawableNode node : nodes) {
            if (node instanceof SourceNode) {
                DataBank.saveInstanceObject(referenceID, node.getContainedText(), ((SourceNode) node).getSource());
            } else if (node != null) {
                DataBank.saveInstanceObject(referenceID, node.getContainedText(), node);
            }
        }
    }

    public Program getParentProgram() {
        return this.parentProgram;
    }

    public List<DrawableNode> getClickedNodes(Double x, Double y) {
        List<DrawableNode> nodeList = new ArrayList<>();

        for (DrawableNode node : nodes) {
            if (node.isCoordInside(x, y)) {
                nodeList.add(node);
            }
        }

        return nodeList;
    }

    public DrawableNode getNodeById(Integer id) {
        for (DrawableNode node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
        }

        return null;
    }

    public void checkConnections() {
        Boolean updateCanvas = false;

        // Find new connections set within a node and creates them
        for (DrawableNode startNode : nodes) {
            if (startNode instanceof SourceNode) {
                String src = ((SourceNode) startNode).getSource().getSource();

                for (DrawableNode endNode : getNodes()) {
                    if (src.contains("run(\"" + endNode.getContainedText() + "\"") || src.contains("runAndWait(\"" + endNode.getContainedText() + "\"")) {
                        if (!connectionExists(startNode, endNode)) {
                            NodeConnection newConnection = new NodeConnection(startNode, endNode, NodeConnection.DYNAMIC_CONNECTION);
                            connections.add(newConnection);
                            updateCanvas = true;
                        }
                    }
                }
            } else if (startNode instanceof SwitchNode) {
                List<Switch> aSwitches = ((SwitchNode) startNode).getSwitches();

                for (DrawableNode endNode : getNodes()) {
                    Boolean createConnection = false;

                    for (Switch aSwitch : aSwitches) {
                        if ((aSwitch.getTarget().equals(endNode.getContainedText()) && aSwitch.isEnabled())) {
                            createConnection = true;
                        }
                    }

                    if (createConnection && !connectionExists(startNode, endNode)) {
                        NodeConnection newConnection = new NodeConnection(startNode, endNode, NodeConnection.DYNAMIC_CONNECTION);
                        connections.add(newConnection);
                        updateCanvas = true;
                    }
                }
            } else if (startNode instanceof LinuxNode) {
                String consoleName = ((LinuxNode) startNode).getConsoleName();

                for (DrawableNode endNode : getNodes()) {
                    Boolean createConnection = false;

                    if (consoleName.equals(endNode.getContainedText())) {
                        createConnection = true;
                    }

                    if (createConnection && !connectionExists(startNode, endNode)) {
                        NodeConnection newConnection = new NodeConnection(startNode, endNode, NodeConnection.DYNAMIC_CONNECTION);
                        connections.add(newConnection);
                        updateCanvas = true;
                    }
                }
            }

            // Find connection that are set using the Next node input box
            if (!startNode.getNextNodeToRun().isEmpty()) {
                for (DrawableNode endNode : getNodes()) {
                    if (startNode.getNextNodeToRun().equals(endNode.getContainedText())) {
                        if (!connectionExists(startNode, endNode)) {
                            NodeConnection newConnection = new NodeConnection(startNode, endNode, NodeConnection.MAIN_CONNECTION);
                            connections.add(newConnection);
                            updateCanvas = true;
                        }
                    }
                }
            }
        }

        // Checks old connections and removes ones that don't exist
        List<NodeConnection> listToRemove = new ArrayList<>();
        for (NodeConnection nodeConnection : connections) {
            if (nodeConnection.getConnectionType().equals(NodeConnection.DYNAMIC_CONNECTION)) {
                if (nodeConnection.getConnectionStart() instanceof SourceNode) {
                    if (!((SourceNode) nodeConnection.getConnectionStart()).getSource().getSource().contains("run(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                        if (!((SourceNode) nodeConnection.getConnectionStart()).getSource().getSource().contains("runAndWait(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                            listToRemove.add(nodeConnection);
                            updateCanvas = true;
                        }
                    }
                } else if (nodeConnection.getConnectionStart() instanceof SwitchNode) {
                    List<Switch> aSwitches = ((SwitchNode) nodeConnection.getConnectionStart()).getSwitches();
                    String endContainedText = nodeConnection.getConnectionEnd().getContainedText();
                    Integer removeCount = 0;
                    for (Switch aSwitch : aSwitches) {
                        if ((!aSwitch.getTarget().equals(endContainedText) || !aSwitch.isEnabled())) {
                            removeCount++;
                        }
                    }

                    if (removeCount.equals(aSwitches.size())) {
                        listToRemove.add(nodeConnection);
                        updateCanvas = true;
                    }
                } else if (nodeConnection.getConnectionStart() instanceof LinuxNode) {
                    String consoleName = ((LinuxNode) nodeConnection.getConnectionStart()).getConsoleName();
                    String endContainedText = nodeConnection.getConnectionEnd().getContainedText();

                    if ((!consoleName.equals(endContainedText))) {
                        listToRemove.add(nodeConnection);
                        updateCanvas = true;
                    }
                }
            }

            if (nodeConnection.getConnectionType().equals(NodeConnection.MAIN_CONNECTION)) {
                if (!nodeConnection.getConnectionStart().getNextNodeToRun().equals(nodeConnection.getConnectionEnd().getContainedText())) {
                    listToRemove.add(nodeConnection);
                    updateCanvas = true;
                }
            }
        }

        connections.removeAll(listToRemove);
        if (updateCanvas) {
            Controller.getInstance().updateCanvasControllerLater();
        }
    }

    public Boolean connectionExists(DrawableNode start, DrawableNode end) {
        for (NodeConnection nodeConnection : connections) {
            if (nodeConnection.getConnectionStart() == start && nodeConnection.getConnectionEnd() == end) {
                return true;
            }
        }

        return false;
    }

    public NodeConnection getConnection(DrawableNode start, DrawableNode end) {
        for (NodeConnection nodeConnection : connections) {
            if (nodeConnection.getConnectionStart() == start && nodeConnection.getConnectionEnd() == end) {
                return nodeConnection;
            }
        }

        return null;
    }

    public static void sourceStarted(String reference) {
        SourceNode sourceNode = FlowController.getSourceFromReference(reference);
        sourceNode.setColor(Color.RED);
        Controller.getInstance().updateCanvasControllerLater();
    }

    public static void sourceFinished(String reference) {
        SourceNode sourceNode = FlowController.getSourceFromReference(reference);
        sourceNode.setColor(Color.BLACK);
        Controller.getInstance().updateCanvasControllerLater();
    }

    public void setSourceToBlack() {
        for (DrawableNode node : nodes) {
            node.setColor(Color.BLACK);
        }
        Controller.getInstance().updateCanvasControllerLater();
    }

    public static DrawableNode getNodeFromContainedText(String text) {
        for (Program program : DataBank.getPrograms()) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node.getContainedText().equals(text)) {
                    return node;
                }
            }
        }

        return null;
    }

    public static FlowController getFlowControllerFromReference(String flowControllerReferenceId) {
        for (Program program : DataBank.getPrograms()) {
            if (program.getFlowController().getReferenceID().equals(flowControllerReferenceId)) {
                return program.getFlowController();
            }
        }

        return null;
    }

    public static FlowController getFlowControllerFromSource(Source source) {
        for (Program program : DataBank.getPrograms()) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node instanceof SourceNode) {
                    if (((SourceNode) node).getSource() == source) {
                        return program.getFlowController();
                    }
                }
            }
        }

        return null;
    }

    public static FlowController getFlowControllerFromBash(Bash bash) {
        for (Program program : DataBank.getPrograms()) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node instanceof BashNode) {
                    if (((BashNode) node).getBash() == bash) {
                        return program.getFlowController();
                    }
                }
            }
        }

        return null;
    }

    public static SourceNode getSourceFromReference(String reference) {
        for (Program program : DataBank.getPrograms()) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node.getId().toString().equals(reference)) {
                    return (SourceNode) node;
                }
            }
        }

        return null;
    }

    public void addActiveConnection(NodeConnection nodeConnection) {
        activeConnections.add(nodeConnection);
        triggerActiveTimer();
    }

    public DrawableNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(DrawableNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public void triggerActiveTimer() {
        if (currentTimer == null) {
            currentTimer = new Timer();  //At this line a new Thread will be created
            currentTimer.schedule(new ActiveRefreshTimer(), 30); //delay in milliseconds
        }
    }

    class ActiveRefreshTimer extends TimerTask {
        @Override
        public void run() {
            currentTimer.cancel();
            List<NodeConnection> removalList = new ArrayList<>();
            for (NodeConnection connection : activeConnections) {
                connection.degradeGradient();
                if (!connection.isTriggeredGradient()) {
                    removalList.add(connection);
                }
            }

            activeConnections.removeAll(removalList);

            Controller.getInstance().updateCanvasControllerLater();

            currentTimer = null;

            if (activeConnections.size() > 0) {
                triggerActiveTimer();
            }
        }
    }
}
