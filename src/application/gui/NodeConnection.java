package application.gui;

import application.node.DrawableNode;
import javafx.scene.paint.Color;

public class NodeConnection {
    private DrawableNode connectionStart;
    private DrawableNode connectionEnd;
    private Integer connectionType = MAIN_CONNECTION;
    private Double gradientFramesRemainingRatio = 0.0;  // goes from 1.0 to 0.0

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

    public Color getBaseColor() {
        if (connectionType.equals(NodeConnection.MAIN_CONNECTION)) {
            return Color.BLACK;
        } else if (connectionType.equals(NodeConnection.DYNAMIC_CONNECTION)) {
            return Color.GRAY;
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
