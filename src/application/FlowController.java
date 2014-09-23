package application;

import application.tester.TestResultNode;
import application.utils.DataBank;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class FlowController {
    private DrawableNode startNode;
    private List<DrawableNode> nodes = new ArrayList<DrawableNode>();
    private List<NodeConnection> connections = new ArrayList<NodeConnection>();
    private String referenceID = "test";
    private Program parentProgram;

    public FlowController(Program parentProgram) {
        this.parentProgram = parentProgram;
        startNode = new SourceNode(30.0, 30.0, "Start");
        startNode.setId(-1);
    }

    public DrawableNode createNewNode(Integer id, Integer programId, String nodeType, Boolean isStartNode) {
        DrawableNode newNode = null;
        if ("TestResultNode".equals(nodeType)) {
            newNode = new TestResultNode(id, programId);
        } else if ("SourceNode".equals(nodeType)) {
            newNode = new SourceNode(id, programId);
        } else if ("SwitchNode".equals(nodeType)) {
            newNode = new SwitchNode(id, programId);
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
        ArrayList<NodeConnection> connectionsToRemove = new ArrayList<NodeConnection>();
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
            } else if (node instanceof TestResultNode) {
                DataBank.saveInstanceObject(referenceID, node.getContainedText(), node);
            } else if (node instanceof SwitchNode) {
                DataBank.saveInstanceObject(referenceID, node.getContainedText(), node);
            }
        }
    }

    public Program getParentProgram() {
        return this.parentProgram;
    }

    public List<DrawableNode> getClickedNodes(Double x, Double y) {
        List<DrawableNode> nodeList = new ArrayList<DrawableNode>();

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

        // Find new connections and creates them
        for (DrawableNode startNode : nodes) {
            if (startNode instanceof SourceNode) {
                String src = ((SourceNode) startNode).getSource().getSource();

                for (DrawableNode endNode : getNodes()) {
                    if (src.contains("run(\"" + endNode.getContainedText() + "\"")) {
                        if (!connectionExists(startNode, endNode)) {
                            NodeConnection newConnection = new NodeConnection(startNode, endNode);
                            connections.add(newConnection);
                            updateCanvas = true;
                        }
                    }
                }
            } else if (startNode instanceof SwitchNode) {
                for (DrawableNode endNode : getNodes()) {
                    List<Switch> aSwitches = ((SwitchNode) startNode).getSwitches();

                    Boolean createConnection = false;
                    for (Switch aSwitch : aSwitches) {
                        if ((aSwitch.getTarget().equals(endNode.getContainedText()) && aSwitch.isEnabled())) {
                            createConnection = true;
                        }
                    }

                    if (createConnection && !connectionExists(startNode, endNode)) {
                        NodeConnection newConnection = new NodeConnection(startNode, endNode);
                        connections.add(newConnection);
                        updateCanvas = true;
                    }
                }
            }
        }

        // Checks old connections and removes ones that don't exist
        List<NodeConnection> listToRemove = new ArrayList<NodeConnection>();
        for (NodeConnection nodeConnection : connections) {
            if (nodeConnection.getConnectionStart() instanceof SourceNode) {
                if (!((SourceNode) nodeConnection.getConnectionStart()).getSource().getSource().contains("run(\"" + nodeConnection.getConnectionEnd().getContainedText() + "\"")) {
                    listToRemove.add(nodeConnection);
                    updateCanvas = true;
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

    public static SourceNode getSourceFromContainedText(String text) {
        for (Program program : DataBank.getPrograms()) {
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node.getContainedText().equals(text)) {
                    return (SourceNode) node;
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
}
