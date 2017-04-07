package sde.application.gui;

import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import sde.application.data.DataBank;
import sde.application.data.model.dao.DAO;
import sde.application.data.model.dao.ProgramDAO;
import sde.application.node.design.DrawableNode;
import sde.application.node.implementations.*;
import sde.application.node.objects.Bash;
import sde.application.node.objects.Logic;
import sde.application.node.objects.Switch;
import sde.application.node.objects.Trigger;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEThread;
import sde.application.utils.managers.SessionManager;

import java.util.*;
import java.util.stream.Collectors;

public class FlowController {
    private static Logger log = Logger.getLogger(FlowController.class);
    private DrawableNode startNode;
    private List<DrawableNode> selectedNodes = new ArrayList<>();
    private List<DrawableNode> nodes = new ArrayList<>();
    private List<NodeConnection> connections = new ArrayList<>();
    private List<NodeConnection> activeConnections = Collections.synchronizedList(new ArrayList<>());
    private List<Trigger> activeTriggers = new ArrayList<>();
    private Program parentProgram;
    private ActiveRefreshTimer activeRefreshTimer = null;
    private Timer currentTimer;
    private Double viewOffsetWidth = 0d;
    private Double viewOffsetHeight = 0d;

    public FlowController(Program parentProgram) {
        this.parentProgram = parentProgram;

        startNode = LogicNode.create(LogicNode.class);
        startNode.setX(30.0);
        startNode.setY(30.0);
        startNode.setContainedText("Start");
    }

    public static void sourceStarted(String uuid) {
        LogicNode logicNode = LogicNode.load(DAO.UUIDFromString(uuid), LogicNode.class);
        logicNode.setColor(Color.RED);
        Controller.getInstance().updateCanvasControllerLater();
    }

    public static void sourceFinished(String uuid) {
        LogicNode logicNode = LogicNode.load(DAO.UUIDFromString(uuid), LogicNode.class);
        logicNode.setColor(Color.BLACK);
        Controller.getInstance().updateCanvasControllerLater();
    }

    public static DrawableNode getNodeFromContainedText(String text) {
        ProgramDAO programDAO = new ProgramDAO();
        List<Program> programs = programDAO.getProgramsByUser(SessionManager.getInstance().getCurrentSession().getUser());

        for (Program program : programs) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node.getContainedText().equals(text)) {
                    return node;
                }
            }
        }

        return null;
    }

    public static FlowController getFlowControllerFromReference(String flowControllerReferenceId) {
        ProgramDAO programDAO = new ProgramDAO();
        List<Program> programs = programDAO.getProgramsByUser(SessionManager.getInstance().getCurrentSession().getUser());

        for (Program program : programs) {
            if (program.getFlowController().getReferenceID().equals(flowControllerReferenceId)) {
                return program.getFlowController();
            }
        }

        return null;
    }

    public static FlowController getFlowControllerFromNode(DrawableNode nodeToFind) {
        ProgramDAO programDAO = new ProgramDAO();
        List<Program> programs = programDAO.getProgramsByUser(SessionManager.getInstance().getCurrentSession().getUser());

        for (Program program : programs) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (nodeToFind.equals(node)) {
                    return program.getFlowController();
                }
            }
        }

        return null;
    }

    public static FlowController getFlowControllerFromLogic(Logic logic) {
        ProgramDAO programDAO = new ProgramDAO();
        List<Program> programs = programDAO.getProgramsByUser(SessionManager.getInstance().getCurrentSession().getUser());

        for (Program program : programs) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node instanceof LogicNode) {
                    if (((LogicNode) node).getLogic() == logic) {
                        return program.getFlowController();
                    }
                }
            }
        }

        return null;
    }

    public static FlowController getFlowControllerFromBash(Bash bash) {
        ProgramDAO programDAO = new ProgramDAO();
        List<Program> programs = programDAO.getProgramsByUser(SessionManager.getInstance().getCurrentSession().getUser());

        for (Program program : programs) {
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

    public static LogicNode getSourceFromReference(String reference) {
        ProgramDAO programDAO = new ProgramDAO();
        List<Program> programs = programDAO.getProgramsByUser(SessionManager.getInstance().getCurrentSession().getUser());

        for (Program program : programs) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node.getUuidString().equals(reference)) {
                    return (LogicNode) node;
                }
            }
        }

        return null;
    }

    public void addNode(DrawableNode drawableNode) {
        drawableNode.setProgram(parentProgram);
        nodes.add(drawableNode);
    }

    public void clearNodes() {
        nodes.clear();
    }

    public void addNodes(List<DrawableNode> drawableNodes) {
        drawableNodes.forEach(this::addNode);
    }

    public void removeNode(DrawableNode drawableNode) {
        nodes.remove(drawableNode);
        ArrayList<NodeConnection> connectionsToRemove = new ArrayList<>();
        for (NodeConnection connection : connections) {
            if (connection.getConnectionStart() == drawableNode || connection.getConnectionEnd() == drawableNode) {
                connectionsToRemove.add(connection);
            }
        }

        connectionsToRemove.forEach(connections::remove);
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
        if (parentProgram != null) {
            return parentProgram.getUuidString();
        }
        return null;
    }

    public Boolean checkIfTreeIsCompiled() {
        Boolean result = true;

        for (DrawableNode node : nodes) {
            if (node instanceof LogicNode) {
                result = ((LogicNode) node).getLogic().isCompiled();
                if (!result) {
                    break;
                }
            } else if (node instanceof TestCaseNode) {
                result = ((TestCaseNode) node).getLogic().isCompiled();
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

        List<SDEThread> compileThreads = new ArrayList<>();
        for (DrawableNode node : nodes) {
            if (node instanceof LogicNode || node instanceof TestCaseNode) {
                compileThreads.add(new SDEThread(new CompileTaskRunnable(node), "Compile Task for for program - " + node.getContainedText(), null, true));
            }
        }

        // Joins us to all compile threads and waits for them to end before continuing
        compileThreads.forEach(SDEThread::join);

        return true; // This should return what the actual compile method returns..
    }

    public void loadInstances() {
        activeTriggers.clear();

        for (DrawableNode node : nodes) {
            // This adds the runnable node objects
            if (node instanceof LogicNode) {
                DataBank.saveInstanceObject(getReferenceID(), node.getContainedText(), ((LogicNode) node).getLogic());
            } else if (node != null) {
                DataBank.saveInstanceObject(getReferenceID(), node.getContainedText(), node);
            }

            // Adds the triggers being used
            if (node instanceof TriggerNode) {
                activeTriggers.addAll(((TriggerNode) node).getTriggers());
            }
        }
    }

    public Program getParentProgram() {
        return this.parentProgram;
    }

    public List<DrawableNode> getClickedNodes(Double x, Double y) {
        List<DrawableNode> clickedNodeList = new ArrayList<>();

        for (DrawableNode node : nodes) {
            if (node.isCoordinateInside(x, y)) {
                clickedNodeList.add(node);
            }
        }

        return clickedNodeList;
    }

    // This returns all the nodes within a bounded box with top left corner (x,y) and bottom right corner (x2,y2)
    public List<DrawableNode> getGroupSelectedNodes(Double x, Double y, Double x2, Double y2) {
        List<DrawableNode> selectedNodeList = new ArrayList<>();

        for (DrawableNode node : nodes) {
            if (x < node.getX() && x2 > node.getX()) {
                if (y < node.getY() && y2 > node.getY()) {
                    selectedNodeList.add(node);
                }
            }
        }

        return selectedNodeList;
    }

    public DrawableNode getNodeByUuidWithoutHyphen(String uuid) {
        for (DrawableNode node : nodes) {
            if (node.getUuidStringWithoutHyphen().equals(uuid)) {
                return node;
            }
        }

        return null;
    }

    public DrawableNode getNodeById(String uuid) {
        for (DrawableNode node : nodes) {
            if (node.getUuidString().equals(uuid)) {
                return node;
            }
        }

        return null;
    }

    // Controls the creation and removal of all node connections displayed on the program flow
    public void checkConnections() {
        Boolean updateCanvas = false;

        // Find new connections set within a node and creates them
        for (DrawableNode node : nodes) {
            if (node instanceof LogicNode) {
                String src = ((LogicNode) node).getLogic().getLogic();

                for (DrawableNode endNode : getNodes()) {
                    int nodeConnectionType = NodeConnection.NO_CONNECTION;

                    // Here we are checked to see if any connections are linked from this LogicNode
                    if (src.contains("run(\"" + endNode.getContainedText() + "\"") ||
                            src.contains("runAndWait(\"" + endNode.getContainedText() + "\"") ||
                            src.contains("runAndJoin(\"" + endNode.getContainedText() + "\"")) {
                        nodeConnectionType = NodeConnection.DYNAMIC_CONNECTION;
                    } else if (src.contains("getNode(\"" + endNode.getContainedText() + "\"")) {
                        nodeConnectionType = NodeConnection.GET_NODE_CONNECTION;
                    }

                    // If we find a possible connection and it doesn't already exist, we create that connection with the correct type
                    if (!connectionExists(node, endNode) && nodeConnectionType != -1) {
                        NodeConnection newConnection = new NodeConnection(node, endNode, nodeConnectionType);
                        connections.add(newConnection);
                        updateCanvas = true;
                    }
                }
            } else if (node instanceof TestCaseNode) {
                String src = ((TestCaseNode) node).getLogic().getLogic();

                for (DrawableNode endNode : getNodes()) {
                    int nodeConnectionType = NodeConnection.NO_CONNECTION;

                    // Here we are checked to see if any connections are linked from this TestCaseNode
                    if (src.contains("run(\"" + endNode.getContainedText() + "\"") ||
                            src.contains("runAndWait(\"" + endNode.getContainedText() + "\"") ||
                            src.contains("runAndJoin(\"" + endNode.getContainedText() + "\"")) {
                        nodeConnectionType = NodeConnection.DYNAMIC_CONNECTION;
                    } else if (src.contains("getNode(\"" + endNode.getContainedText() + "\"")) {
                        nodeConnectionType = NodeConnection.GET_NODE_CONNECTION;
                    }

                    // If we find a possible connection and it doesn't already exist, we create that connection with the correct type
                    if (!connectionExists(node, endNode) && nodeConnectionType != -1) {
                        NodeConnection newConnection = new NodeConnection(node, endNode, nodeConnectionType);
                        connections.add(newConnection);
                        updateCanvas = true;
                    }
                }
            } else if (node instanceof SwitchNode) {
                List<Switch> aSwitches = ((SwitchNode) node).getSwitches();

                for (DrawableNode endNode : getNodes()) {
                    Boolean createConnection = false;
                    Boolean enabledConnection = false;

                    for (Switch aSwitch : aSwitches) {
                        if ((aSwitch.getTarget().equals(endNode.getContainedText()))) {
                            enabledConnection = aSwitch.isEnabled();
                            createConnection = true;
                        }
                    }

                    if (createConnection && !connectionExists(node, endNode)) {
                        Integer connectionType = NodeConnection.DYNAMIC_CONNECTION;
                        if (!enabledConnection) {
                            connectionType = NodeConnection.DISABLED_CONNECTION;
                        }

                        NodeConnection newConnection = new NodeConnection(node, endNode, connectionType);
                        connections.add(newConnection);
                        updateCanvas = true;
                    }
                }
            } else if (node instanceof TriggerNode) {
                List<Trigger> triggers = ((TriggerNode) node).getTriggers();

                for (Trigger trigger : triggers) {
                    String watchName = trigger.getWatch();

                    for (DrawableNode endNode : getNodes()) {
                        Boolean createConnection = false;

                        if (watchName.equals(endNode.getContainedText())) {
                            createConnection = true;
                        }

                        // This connection has the start and end the other way around, the target is specified in the trigger but
                        // we want it to look like the watched node is connecting to the trigger as that is the order that they are run
                        if (createConnection && !connectionExists(endNode, node)) {
                            NodeConnection newConnection = new NodeConnection(endNode, node, NodeConnection.TRIGGER_CONNECTION);
                            connections.add(newConnection);
                            updateCanvas = true;
                        }
                    }
                }
            }

            // Find connection that are set using the Next node input box
            if (node != null && !node.getNextNodeToRun().isEmpty()) {
                for (DrawableNode endNode : getNodes()) {
                    if (node.getNextNodeToRun().equals(endNode.getContainedText())) {
                        if (!connectionExists(node, endNode)) {
                            NodeConnection newConnection = new NodeConnection(node, endNode, NodeConnection.MAIN_CONNECTION);
                            connections.add(newConnection);
                            updateCanvas = true;
                        }
                    }
                }
            }
        }

        // Checks old connections and removes ones that don't exist
        List<NodeConnection> listToRemove = new ArrayList<>();
        List<NodeConnection> connectionsLoopTemp = new ArrayList<>(); // We make a copy the list here so that we can the original while iterating over this one
        connectionsLoopTemp.addAll(connections);
        for (NodeConnection nodeConnection : connectionsLoopTemp) {
            if (nodeConnection.getConnectionType().equals(NodeConnection.DYNAMIC_CONNECTION)) {
                if (nodeConnection.getConnectionStart() instanceof LogicNode) {
                    if (!((LogicNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("run(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                        if (!((LogicNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("runAndWait(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                            if (!((LogicNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("runAndJoin(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                                if (!((LogicNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("getNode(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                                    listToRemove.add(nodeConnection);
                                    updateCanvas = true;
                                }
                            }
                        }
                    }
                } else if (nodeConnection.getConnectionStart() instanceof TestCaseNode) {
                    if (!((TestCaseNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("run(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                        if (!((TestCaseNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("runAndWait(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                            if (!((TestCaseNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("runAndJoin(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                                if (!((TestCaseNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("getNode(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                                    listToRemove.add(nodeConnection);
                                    updateCanvas = true;
                                }
                            }
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
                        // Create a disabled connection between these two nodes to give appearance of toggling
                        NodeConnection newConnection = new NodeConnection(nodeConnection.getConnectionStart(), nodeConnection.getConnectionEnd(), NodeConnection.DISABLED_CONNECTION);
                        connections.add(newConnection);

                        listToRemove.add(nodeConnection);
                        updateCanvas = true;
                    }
                }
            } else if (nodeConnection.getConnectionType().equals(NodeConnection.TRIGGER_CONNECTION)) {
                if (nodeConnection.getConnectionEnd() instanceof TriggerNode) { // Here the start and end connections are reversed
                    List<Trigger> triggers = ((TriggerNode) nodeConnection.getConnectionEnd()).getTriggers();
                    String endContainedText = nodeConnection.getConnectionStart().getContainedText();
                    Integer removeCount = 0;
                    for (Trigger trigger : triggers) {
                        if ((!trigger.getWatch().equals(endContainedText))) {
                            removeCount++;
                        }
                    }

                    if (removeCount.equals(triggers.size())) {
                        listToRemove.add(nodeConnection);
                        updateCanvas = true;
                    }
                }
            } else if (nodeConnection.getConnectionType().equals(NodeConnection.GET_NODE_CONNECTION)) {
                if (nodeConnection.getConnectionStart() instanceof LogicNode) {
                    if (!((LogicNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("getNode(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                        listToRemove.add(nodeConnection);
                        updateCanvas = true;
                    }
                } else if (nodeConnection.getConnectionStart() instanceof TestCaseNode) {
                    if (!((TestCaseNode) nodeConnection.getConnectionStart()).getLogic().getLogic().contains("getNode(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                        listToRemove.add(nodeConnection);
                        updateCanvas = true;
                    }
                }
            } else if (nodeConnection.getConnectionType().equals(NodeConnection.DISABLED_CONNECTION)) {
                if (nodeConnection.getConnectionStart() instanceof SwitchNode) {
                    List<Switch> aSwitches = ((SwitchNode) nodeConnection.getConnectionStart()).getSwitches();
                    String endContainedText = nodeConnection.getConnectionEnd().getContainedText();
                    Integer removeCount = 0;
                    for (Switch aSwitch : aSwitches) {
                        if ((!aSwitch.getTarget().equals(endContainedText) || aSwitch.isEnabled())) {
                            removeCount++;
                        }
                    }

                    if (removeCount.equals(aSwitches.size())) {
                        // Create an enabled connection between these two nodes to give appearance of toggling
                        NodeConnection newConnection = new NodeConnection(nodeConnection.getConnectionStart(), nodeConnection.getConnectionEnd(), NodeConnection.DYNAMIC_CONNECTION);
                        connections.add(newConnection);

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

    public List<NodeConnection> getConnections(DrawableNode node) {
        List<NodeConnection> nodeConnections = connections.stream().filter(nodeConnection -> nodeConnection.getConnectionStart() == node || nodeConnection.getConnectionEnd() == node).collect(Collectors.toList());

        return nodeConnections;
    }

    public NodeConnection getConnection(DrawableNode start, DrawableNode end) {
        for (NodeConnection nodeConnection : connections) {
            if (nodeConnection.getConnectionStart() == start && nodeConnection.getConnectionEnd() == end) {
                return nodeConnection;
            }
        }

        return null;
    }

    public void setSourceToBlack() {
        for (DrawableNode node : nodes) {
            node.setColor(Color.BLACK);
        }
        Controller.getInstance().updateCanvasControllerLater();
    }

    public DrawableNode getNodeThisControllerFromContainedText(String text) {
        for (DrawableNode node : getNodes()) {
            if (node.getContainedText().equals(text)) {
                return node;
            }
        }

        return null;
    }

    public void addActiveConnection(NodeConnection nodeConnection) {
        activeConnections.add(nodeConnection);
        triggerActiveTimer();
    }

    public List<DrawableNode> getSelectedNodes() {
        return selectedNodes;
    }

    public void setSelectedNodes(List<DrawableNode> selectedNodes) {
        this.selectedNodes = selectedNodes;
    }

    public void triggerActiveTimer() {
        if (currentTimer == null) {
            currentTimer = new Timer();  //At this line a new Thread will be created
            currentTimer.schedule(new ActiveRefreshTimer(), 30); //delay in milliseconds
        }
    }

    public Double getViewOffsetHeight() {
        return viewOffsetHeight;
    }

    public void setViewOffsetHeight(Double viewOffsetHeight) {
        if (viewOffsetHeight == null) {
            this.viewOffsetHeight = 0d;
        } else {
            this.viewOffsetHeight = viewOffsetHeight;
        }
    }

    public Double getViewOffsetWidth() {
        return viewOffsetWidth;
    }

    public void setViewOffsetWidth(Double viewOffsetWidth) {
        if (viewOffsetWidth == null) {
            this.viewOffsetWidth = 0d;
        } else {
            this.viewOffsetWidth = viewOffsetWidth;
        }
    }

    // Returns a list of triggers based on a node's name and trigger when that the trigger is currently watching.
    public List<Trigger> getActiveTriggers(String containedText, String triggerWhen) {
        return activeTriggers.stream().filter(trigger -> trigger.getWatch().equals(containedText) && trigger.getWhen().equals(triggerWhen)).collect(Collectors.toList());
    }

    // Gets all active triggers
    public List<Trigger> getActiveTriggers() {
        return activeTriggers;
    }

    private class CompileTaskRunnable extends SDERunnable {
        private DrawableNode node;
        private Logic logic;

        CompileTaskRunnable(DrawableNode node) {
            this.node = node;
            if (node instanceof LogicNode) {
                this.logic = ((LogicNode) node).getLogic();
            } else if (node instanceof TestCaseNode) {
                this.logic = ((TestCaseNode) node).getLogic();
            }
        }

        public void threadRun() {
            Boolean result = logic.compile();

            if (result) {
                node.setColor(Color.LIMEGREEN);
            } else {
                node.setColor(Color.ORANGE);
            }

            Controller.getInstance().updateCanvasControllerLater();
        }
    }

    class ActiveRefreshTimer extends TimerTask {
        @Override
        public synchronized void run() {
            if (currentTimer != null) {
                currentTimer.cancel();
            }
            List<NodeConnection> removalList = new ArrayList<>();
            // We copy the list here to avoid concurrent modification
            List<NodeConnection> activeConnectionsCopy = new ArrayList<>();
            activeConnectionsCopy.addAll(activeConnections);

            for (NodeConnection connection : activeConnectionsCopy) {
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
