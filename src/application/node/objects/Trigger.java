package application.node.objects;

import application.data.model.DatabaseObject;
import application.node.implementations.TriggerNode;

public class Trigger extends DatabaseObject {
    private String watch = "";
    private String when = "";
    private String then = "";
    private TriggerNode parent;

    public Trigger(Trigger copyTrigger, TriggerNode parent) {
        super(-1);
        this.watch = copyTrigger.getWatch();
        this.when = copyTrigger.getWhen();
        this.then = copyTrigger.getThen();
        this.parent = parent;
    }

    public Trigger(Integer id, String watch, String when, String then, TriggerNode parent) {
        super(id);
        this.watch = watch;
        this.when = when;
        this.then = then;
        this.parent = parent;
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
        return parent;
    }

    public Integer getParentId() {
        return parent.getId();
    }

    public void setParent(TriggerNode parent) {
        this.parent = parent;
    }
}
