package application.gui;

import application.node.DrawableNode;

public class NodeConnection {
    private DrawableNode connectionStart;
    private DrawableNode connectionEnd;

    public NodeConnection(DrawableNode connectionStart, DrawableNode connectionEnd) {
        this.connectionEnd = connectionEnd;
        this.connectionStart = connectionStart;
    }

    public DrawableNode getConnectionStart() {
        return this.connectionStart;
    }

    public DrawableNode getConnectionEnd() {
        return this.connectionEnd;
    }
}
