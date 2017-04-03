package application.gui;

import application.data.DataBank;
import application.data.User;
import application.data.model.DatabaseObject;
import application.data.model.dao.DrawableNodeDAO;
import application.gui.dialog.ErrorDialog;
import application.node.design.DrawableNode;
import application.node.implementations.LogicNode;
import application.node.objects.Logic;
import application.utils.NodeRunParams;
import application.utils.SDERunnable;
import application.utils.SDEThread;
import application.utils.managers.SessionManager;
import application.utils.managers.ThreadManager;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.UUID;

public class Program extends DatabaseObject implements Comparable<Program> {
    private static Logger log = Logger.getLogger(Program.class);
    private String name;
    private FlowController flowController = new FlowController(this);
    private Boolean locked = false;
    private User parentUser;

    public Program() {
        super();
    }

    public static SDEThread runHelper(String name, String referenceID, DrawableNode sourceNode, Boolean whileWaiting, Boolean startImmediately, String threadReference, NodeRunParams nodeRunParams) {
        class OneShotTask implements Runnable {
            OneShotTask() {
            }

            public void run() {
                Object node = DataBank.getInstanceObject(referenceID, name);
                if (node instanceof Logic) {
                    Logic logic = ((Logic) node);
                    triggerConnections(sourceNode, ((Logic) node).getParentLogicNode().getContainedText(), referenceID);
                    logic.run(whileWaiting, nodeRunParams, threadReference);
                } else if (node instanceof DrawableNode) {
                    DrawableNode drawableNode = (DrawableNode) node;
                    triggerConnections(sourceNode, ((DrawableNode) node).getContainedText(), referenceID);
                    drawableNode.run(whileWaiting, nodeRunParams);
                } else {
                    log.info("Unable to run the node '" + name + "'");
                    if ("Start".equals(name) && "1".equals(referenceID)) {
                        new ErrorDialog()
                                .content("A start node needs to be selected")
                                .title("No start node")
                                .show();
                    } else {
                        new ErrorDialog()
                                .content("Cannot find node '" + name + "'.")
                                .title("Failed to run node")
                                .show();
                    }
                }

                DrawableNode drawableNode = null;
                if (node instanceof DrawableNode) {
                    drawableNode = ((DrawableNode) node);
                } else if (node instanceof Logic) {
                    drawableNode = ((Logic) node).getParentLogicNode();
                }

                // Running any nodes in the next node
                if (drawableNode != null && drawableNode.getNextNodeToRun() != null && !drawableNode.getNextNodeToRun().isEmpty()) {
                    triggerConnections(drawableNode, drawableNode.getNextNodeToRun(), referenceID);
                    Program.runHelper(drawableNode.getNextNodeToRun(), referenceID, drawableNode, true, true, null, nodeRunParams);
                }
            }
        }

        // Starts a new thread for each time this is called.
        SDEThread sdeThread;
        if (sourceNode instanceof LogicNode) { // If this is a logic node
            sdeThread = new SDEThread(new OneShotTask(), "Running " + (sourceNode == null ? "Program - " + SessionManager.getInstance().getCurrentSession().getSelectedProgram().getName() : " Node - " + sourceNode.getContainedText()), null, startImmediately);
        } else { // If something else then add reference if it is there
            sdeThread = new SDEThread(new OneShotTask(), "Running " + (sourceNode == null ? "Program - " + SessionManager.getInstance().getCurrentSession().getSelectedProgram().getName() : " Node - " + sourceNode.getContainedText()), threadReference, startImmediately);
            sdeThread.join(); // Wait here for the thread setup to be done
        }

        // If we need to wait for this thread to finish first we join to the current thread, this is used for runAndWait
        if (whileWaiting) {
//            log.info("THREADING:Starting to wait for '" + sdeThread + "' to finish");
            sdeThread.join();
//            log.info("THREADING:'" + sdeThread + "' has finished");
            ThreadManager.getInstance().removeInactiveThreads(); // Check to see if this thread has finished yet
        }

        return sdeThread;
    }

    // This methods takes a sourceNode where a connection is coming from and a name of a target and triggers the connection to flash on the GUI
    private static void triggerConnections(DrawableNode sourceNode, String targetNodeName, String referenceID) {
        Object node = DataBank.getInstanceObject(referenceID, targetNodeName);
        DrawableNode targetNode = null;
        if (node instanceof Logic) {
            targetNode = ((Logic) node).getParentLogicNode();
        } else if (node instanceof DrawableNode) {
            targetNode = (DrawableNode) node;
        }

        if (sourceNode != null && targetNode != null) {
            FlowController flowController = sourceNode.getProgram().getFlowController();
            NodeConnection nodeConnection = flowController.getConnection(sourceNode, targetNode);
            if (nodeConnection != null) {
                nodeConnection.triggerGradient();
                flowController.addActiveConnection(nodeConnection);
                Controller.getInstance().updateCanvasControllerLater();
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getStartNodeUuid() {
        if (flowController != null && flowController.getStartNode() != null) {
            return flowController.getStartNode().getUuid();
        }
        return null;
    }

    public void setStartNode(DrawableNode startNode) {
        flowController.setStartNode(startNode);
    }

    public Double getViewOffsetHeight() {
        if (flowController != null) {
            return flowController.getViewOffsetHeight();
        }
        return 0.0;
    }

    public void setViewOffsetHeight(Double offset) {
        if (flowController != null) {
            flowController.setViewOffsetHeight(offset);
        }
    }

    public Double getViewOffsetWidth() {
        if (flowController != null) {
            return flowController.getViewOffsetWidth();
        }
        return 0.0;
    }

    public void setViewOffsetWidth(Double offset) {
        if (flowController != null) {
            flowController.setViewOffsetWidth(offset);
        }
    }

    public UUID getParentUserUuid() {
        if (parentUser != null) {
            return parentUser.getUuid();
        }
        return null;
    }

    public void setParentUser(User parentUser) {
        this.parentUser = parentUser;
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

        new SDEThread(new CompileRunnable(), "Compile Thread for program - " + this.getName(), null, true);

        // err this should return what the threaded compile returns but not sure how to do that yet..
        return true;
    }

    public void run() {
        getFlowController().setSourceToBlack();
        getFlowController().loadInstances();

        class RunProgram implements Runnable {
            RunProgram() {
            }

            public void run() {
                if (getFlowController().getStartNode() != null) {
                    getFlowController().compile();
                    Program.runHelper(getFlowController().getStartNode().getContainedText(), getFlowController().getReferenceID(), null, false, true, null, new NodeRunParams());
                } else {
                    log.info("No start node found");
                    new ErrorDialog()
                            .content("Nothing to run, select a start node.")
                            .header("No start node found")
                            .show();
                }
            }
        }

        // Starts the program in a new thread separate from the GUI thread.
        new SDEThread(new RunProgram(), "Running program - " + this.getName(), null, true);
    }

    public List<DrawableNode> getNodes() {
        return flowController.getNodes();
    }

    public void loadNodesToFlowController() {
        DrawableNodeDAO drawableNodeDAO = new DrawableNodeDAO();
        List<DrawableNode> drawableNodes = drawableNodeDAO.getNodes(this);

        getFlowController().clearNodes();
        getFlowController().addNodes(drawableNodes);
        getFlowController().checkConnections();
    }

    public void delete() {
        getNodes().forEach(application.node.design.DrawableNode::delete);
        super.delete();
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    @Override
    public int compareTo(Program program) {
        if (program != null) {
            Object var1 = (program).getName();
            Object var2 = name;

            if (var1 != null && var2 != null) {
                return ((String) var1).compareTo(var2.toString());
            }
        }

        return 0;
    }
}
