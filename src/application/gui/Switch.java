package application.gui;

import application.node.SwitchNode;

public class Switch {
    private Boolean enabled = false;
    private String target;
    private SwitchNode parent;
    private Integer id;

    public Switch(Integer id, SwitchNode parent, String target, Boolean enabled) {
        this.target = target;
        this.id = id;
        this.parent = parent;
        this.enabled = enabled;
    }

    public Boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public SwitchNode getParent() {
        return parent;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
