package application.gui;

import application.data.DataBank;
import application.node.DrawableNode;
import application.utils.SDERunnable;
import application.utils.SDEThread;

import java.util.HashMap;

public class Program {
    private String name;
    private FlowController flowController;
    private Integer id = -1;

    public Program(String name) {
        this.name = name;
        flowController = new FlowController(this);
    }

    public Program(String name, Integer id) {
        this.name = name;
        this.id = id;
        flowController = new FlowController(this);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        DataBank.saveProgram(this);
    }

    public Integer getId() {
        return this.id;
    }

    public FlowController getFlowController() {
        return this.flowController;
    }

    public Boolean isCompiled() {
        return this.flowController.checkIfTreeIsCompiled();
    }

    public Boolean compile() {
        class CompileRunnable extends SDERunnable {
            CompileRunnable() {
            }

            public void threadRun() {
                flowController.compile();
            }
        }

        new SDEThread(new CompileRunnable());

        // err this should return what the threaded compile returns but not sure how to do that yet..
        return true;
    }

    public void run() {
        getFlowController().setSourceToBlack();
        getFlowController().loadInstances();

        class OneShotTask implements Runnable {
            OneShotTask() {
            }

            public void run() {
                Program.runHelper(getFlowController().getStartNode().getContainedText(), getFlowController().getReferenceID(), null, false, true, new HashMap<>());
            }
        }

        // Starts the program in a new thread separate from the GUI thread.
        new SDEThread(new OneShotTask());
    }

    public static void runHelper(String name, String referenceID, DrawableNode sourceNode, Boolean whileWaiting, Boolean main, HashMap<String, Object> map) {
        class OneShotTask implements Runnable {
            OneShotTask() {
            }

            public void run() {
                Object node = DataBank.getInstanceObject(referenceID, name);
                if (node instanceof Source) {
                    Source source = ((Source) node);
                    triggerConnections(sourceNode, ((Source) node).getParentSourceNode().getContainedText(), referenceID);
                    source.run(whileWaiting, map);
                } else if (node instanceof DrawableNode) {
                    DrawableNode drawableNode = (DrawableNode) node;
                    triggerConnections(sourceNode, ((DrawableNode) node).getContainedText(), referenceID);
                    drawableNode.run(whileWaiting, map);
                }

                // Main is only true when using the main path of execution
//              if (main) {
                DrawableNode drawableNode = null;
                if (node instanceof DrawableNode) {
                    drawableNode = ((DrawableNode) node);
                } else if (node instanceof Source) {
                    drawableNode = ((Source) node).getParentSourceNode();
                }

                if (drawableNode != null && drawableNode.getNextNodeToRun() != null && !drawableNode.getNextNodeToRun().isEmpty()) {
                    triggerConnections(drawableNode, drawableNode.getNextNodeToRun(), referenceID);
                    Program.runHelper(drawableNode.getNextNodeToRun(), referenceID, drawableNode, true, main, map);
                }
//              }
            }
        }

        // Starts a new thread for each time this is called.
        new SDEThread(new OneShotTask());
    }

    // This methods takes a sourceNode where a connection is coming from and a name of a target and triggers the connection to flash on the GUI
    private static void triggerConnections(DrawableNode sourceNode, String targetNodeName, String referenceID) {
        FlowController flowController = DataBank.currentlyEditProgram.getFlowController();

        Object node = DataBank.getInstanceObject(referenceID, targetNodeName);
        DrawableNode targetNode = null;
        if (node instanceof Source) {
            targetNode = ((Source) node).getParentSourceNode();
        } else if (node instanceof DrawableNode) {
            targetNode = (DrawableNode) node;
        }

        if (sourceNode != null && targetNode != null) {
            NodeConnection nodeConnection = flowController.getConnection(sourceNode, targetNode);
            if (nodeConnection != null) {
                nodeConnection.triggerGradient();
                flowController.addActiveConnection(nodeConnection);
                Controller.getInstance().updateCanvasControllerLater();
            }
        }
    }

    public String toString() {
        return "" + this.name;
    }
}
