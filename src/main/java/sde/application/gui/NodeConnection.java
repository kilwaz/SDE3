package sde.application.gui;

import sde.application.node.design.DrawableNode;
import javafx.scene.paint.Color;

public class NodeConnection {
    private DrawableNode connectionStart;
    private DrawableNode connectionEnd;
    private Integer connectionType = NO_CONNECTION;
    private Double gradientFramesRemainingRatio = 0.0;  // goes from 1.0 to 0.0

    public static final int NO_CONNECTION = -1; // Used to signify no found connection
    public static final int MAIN_CONNECTION = 0; // Set from changing
    public static final int DYNAMIC_CONNECTION = 1; // Set from within a node (Like run() within SourceNode)
    public static final int TRIGGER_CONNECTION = 2; // Set from triggers watching a node
    public static final int GET_NODE_CONNECTION = 3; // Set from getNode() method from within LogicNode - Used to access node variables and methods
    public static final int DISABLED_CONNECTION = 4; // Set from switch node when a switch is disabled

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

    public Color getBaseColor() {
        switch (connectionType) {
            case 0:
                return Color.BLACK;
            case 1:
                return Color.RED;
            case 2:
                return Color.GREEN;
            case 3:
                return Color.BLUE;
            case 4:
                return Color.LIGHTPINK;
        }

        return Color.BLACK;
    }

    public Boolean isTriggeredGradient() {
        return gradientFramesRemainingRatio > 0;
    }

    public void degradeGradient() {
        gradientFramesRemainingRatio -= 0.05;
    }

    public void triggerGradient() {
        gradientFramesRemainingRatio = 1.0;
    }

    public Color getRunGradientColor() {
        Color initialColor = Color.RED;

        // Here we are calculating the gradient colour depending on the ratio 'gradientFramesRemainingRatio' given.
        int red = (int) (Math.abs((gradientFramesRemainingRatio * initialColor.getRed()) + ((1 - gradientFramesRemainingRatio) * getBaseColor().getRed())) * 255);
        int green = (int) (Math.abs((gradientFramesRemainingRatio * initialColor.getGreen()) + ((1 - gradientFramesRemainingRatio) * getBaseColor().getGreen())) * 255);
        int blue = (int) (Math.abs((gradientFramesRemainingRatio * initialColor.getBlue()) + ((1 - gradientFramesRemainingRatio) * getBaseColor().getBlue())) * 255);

        // The rgb values are then converted to the colour we want to use.
        return Color.rgb(red, green, blue);
    }
}
