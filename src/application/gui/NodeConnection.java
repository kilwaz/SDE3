package application.gui;

import application.node.DrawableNode;

public class NodeConnection {
    private DrawableNode connectionStart;
    private DrawableNode connectionEnd;
    private Integer connectionType = MAIN_CONNECTION;

    public static final int MAIN_CONNECTION = 0; // Set from changing
    public static final int DYNAMIC_CONNECTION = 1; // Set from within a node (Like run() within SourceNode)

    public NodeConnection(DrawableNode connectionStart, DrawableNode connectionEnd, Integer connectionType) {
        this.connectionEnd = connectionEnd;
        this.connectionStart = connectionStart;
        this.connectionType = connectionType;
    }

    public DrawableNode getConnectionStart() {
        return this.connectionStart;
    }

    public DrawableNode getConnectionEnd() {
        return this.connectionEnd;
    }

    public Integer getConnectionType() {
        return connectionType;
    }
}
