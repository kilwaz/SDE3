package application.node.objects;

import application.data.model.DatabaseObject;
import application.node.implementations.SwitchNode;

import java.util.UUID;

public class Switch extends DatabaseObject {
    private Boolean enabled = false;
    private String target;
    private SwitchNode parent;

    public Switch(Switch copySwitch, SwitchNode parent) {
        this.enabled = copySwitch.isEnabled();
        this.target = copySwitch.getTarget();
        this.parent = parent;
    }

    public Switch(UUID uuid, SwitchNode parent, String target, Boolean enabled) {
        super(uuid);
        this.target = target;
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

    public String getParentUuid() {
        if (parent != null) {
            return parent.getUuidString();
        }
        return null;
    }
}
