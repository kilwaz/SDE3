package application.node.objects;

import application.data.DataBank;
import application.node.implementations.BatchNode;

public class Batch {
    private String script;
    private BatchNode parentBatchNode;
    private Integer id = -1;

    public Batch(BatchNode parentBatchNode) {
        this.parentBatchNode = parentBatchNode;
        this.script = "@ECHO off\n" +
                "ECHO Hello World!\n";
    }

    public Batch(BatchNode parentBatchNode, String script, Integer id) {
        this.parentBatchNode = parentBatchNode;
        this.script = script;
        this.id = id;
    }

    public BatchNode getParentBatchNode() {
        return this.parentBatchNode;
    }

    public String getScript() {
        return this.script;
    }

    public void setScript(String script) {
        if (!this.script.equals(script)) {
            this.script = script;
            if (!parentBatchNode.isInitialising()) {
                DataBank.saveNode(parentBatchNode);
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
