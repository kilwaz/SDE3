package application;

import application.utils.DataBank;
import application.utils.ThreadManager;

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
    }

    public int getId() {
        return this.id;
    }

    public FlowController getFlowController() {
        return this.flowController;
    }

    public Boolean isCompiled() {
        return this.flowController.checkIfTreeIsCompiled();
    }

    public Boolean compile() {
        class CompileRunnable implements Runnable {
            CompileRunnable() {
            }

            public void run() {
                flowController.compile();
            }
        }

        Thread t = new Thread(new CompileRunnable());
        ThreadManager.getInstance().addThread(t);
        t.start();

        // err this should return what the threaded compile returns but not sure how to do that yet..
        return true;
    }

    public void run() {
        getFlowController().setSourceToBlack();
        this.flowController.loadInstances();
        ((SourceNode) this.flowController.getStartNode()).run();
    }

    public static void runHelper(String name, String referenceID, Boolean whileWaiting, HashMap<String, Object> map) {
        Object node = DataBank.getInstanceObject(referenceID, name);
        if (node instanceof Source) {
            ((Source) node).run(whileWaiting, map);
        } else if (node instanceof SwitchNode) {
            ((SwitchNode) node).run(whileWaiting, map);
        }
    }

    public String toString() {
        return "" + this.name;
    }
}
