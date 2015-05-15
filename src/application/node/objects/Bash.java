package application.node.objects;

import application.data.DataBank;
import application.node.implementations.BashNode;

public class Bash {
    private String script;
    private BashNode parentBashNode;
    private Integer id = -1;

    public Bash(BashNode parentBashNode) {
        this.parentBashNode = parentBashNode;
        this.script = "#!/bin/bash\n" +
                "VAR=\"hello\"\n" +
                "echo $VAR";
    }

    public Bash(BashNode parentBashNode, String script, Integer id) {
        this.parentBashNode = parentBashNode;
        this.script = script;
        this.id = id;
    }

    public BashNode getParentBashNode() {
        return this.parentBashNode;
    }

    public String getScript() {
        return this.script;
    }

    public void setScript(String script) {
        if (!this.script.equals(script)) {
            this.script = script;
            if (!parentBashNode.isInitialising()) {
                DataBank.saveNode(parentBashNode);
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
