package sde.application.node.objects;

import sde.application.data.model.DatabaseObject;
import sde.application.node.implementations.SwitchNode;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.UUID;

public class Switch extends DatabaseObject {
    private BooleanProperty enabled = new SimpleBooleanProperty(false);
    private StringProperty target = new SimpleStringProperty("");
    private SwitchNode parent;

    public Switch() {
        super();
    }

    public Switch(Switch copySwitch, SwitchNode parent) {
        this.enabled.set(copySwitch.isEnabled());
        this.target.set(copySwitch.getTarget());
        this.parent = parent;
    }

    public Switch(UUID uuid, SwitchNode parent, String target, Boolean enabled) {
        super(uuid);
        this.target.set(target);
        this.parent = parent;
        this.enabled.set(enabled);
    }

    public BooleanProperty isEnabledProp() {
        return enabled;
    }

    public Boolean isEnabled() {
        return enabled.get();
    }

    public void setEnabled(Boolean enabled) {
        this.enabled.set(enabled);
    }

    public StringProperty getTargetProp() {
        return target;
    }

    public String getTarget() {
        return target.get();
    }

    public void setTarget(String target) {
        this.target.set(target);
    }

    public SwitchNode getParent() {
        return parent;
    }

    public void setParent(SwitchNode parent) {
        this.parent = parent;
    }

    public String getParentUuid() {
        if (parent != null) {
            return parent.getUuidString();
        }
        return null;
    }
}
