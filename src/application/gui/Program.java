package application.gui;

import application.data.DataBank;
import application.node.BashNode;
import application.node.DrawableNode;
import application.node.LinuxNode;
import application.node.SwitchNode;
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
        Program.runHelper(getFlowController().getStartNode().getContainedText(), getFlowController().getReferenceID(), false, true, new HashMap<>());
    }

    public static void runHelper(String name, String referenceID, Boolean whileWaiting, Boolean main, HashMap<String, Object> map) {
        Object node = DataBank.getInstanceObject(referenceID, name);
        if (node instanceof Source) {
            ((Source) node).run(whileWaiting, map);
        } else if (node instanceof SwitchNode) {
            ((SwitchNode) node).run(whileWaiting, map);
        } else if (node instanceof LinuxNode) {
            ((LinuxNode) node).run(whileWaiting, map);
        } else if (node instanceof BashNode) {
            ((BashNode) node).run(whileWaiting, map);
        }

        // Main is only true when using the main path of execution
        if (main) {
            DrawableNode drawableNode = null;
            if (node instanceof DrawableNode) {
                drawableNode = ((DrawableNode) node);
            } else if (node instanceof Source) {
                drawableNode = ((Source) node).getParentSourceNode();
            }

            if (!drawableNode.getNextNodeToRun().isEmpty()) {
                Program.runHelper(drawableNode.getNextNodeToRun(), referenceID, true, main, map);
            }
        }
    }

    public String toString() {
        return "" + this.name;
    }
}
