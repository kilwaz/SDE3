package application.node.objects;

import application.node.implementations.TriggerNode;

public class Trigger {
    private Integer id = -1;
    private String watch = "";
    private String when = "";
    private String then = "";
    private TriggerNode parent;

    public Trigger(Trigger copyTrigger, TriggerNode parent) {
        this.watch = copyTrigger.getWatch();
        this.when = copyTrigger.getWhen();
        this.then = copyTrigger.getThen();
        this.parent = parent;
        this.id = -1;
    }

    public Trigger(Integer id, String watch, String when, String then, TriggerNode parent) {
        this.id = id;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TriggerNode getParent() {
        return parent;
    }

    public void setParent(TriggerNode parent) {
        this.parent = parent;
    }
}
