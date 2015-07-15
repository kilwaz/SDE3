package application.node.objects;

import application.node.implementations.SwitchNode;

public class Switch {
    private Integer id;
    private Boolean enabled = false;
    private String target;
    private SwitchNode parent;

    public Switch(Switch copySwitch, SwitchNode parent) {
        this.enabled = copySwitch.isEnabled();
        this.target = copySwitch.getTarget();
        this.parent = parent;
        this.id = -1;
    }

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
