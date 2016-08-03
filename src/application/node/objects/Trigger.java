package application.node.objects;

import application.data.model.DatabaseObject;
import application.node.implementations.TriggerNode;

import java.util.UUID;

public class Trigger extends DatabaseObject {
    private String watch = "";
    private String when = "";
    private String then = "";
    private TriggerNode parentNode;

    public Trigger() {

    }

    public Trigger(Trigger copyTrigger, TriggerNode parent) {
        this.watch = copyTrigger.getWatch();
        this.when = copyTrigger.getWhen();
        this.then = copyTrigger.getThen();
        this.parentNode = parent;
    }

    public Trigger(UUID uuid, String watch, String when, String then, TriggerNode parent) {
        super(uuid);
        this.watch = watch;
        this.when = when;
        this.then = then;
        this.parentNode = parent;
    }

    public String getWatch() {
        return watch;
    }

    public void setWatch(String watch) {
        this.watch = watch;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
    }

    public String getThen() {
        return then;
    }

    public void setThen(String then) {
        this.then = then;
    }

    public TriggerNode getParent() {
        return parentNode;
    }

    public String getParentUuid() {
        if (parentNode != null) {
            return parentNode.getUuidString();
        }

        return null;
    }

    public void setParent(TriggerNode parent) {
        this.parentNode = parent;
    }
}
