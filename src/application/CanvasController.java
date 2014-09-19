package application;

import application.tester.TestResultNode;
import application.utils.DataBank;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class CanvasController {
    private Canvas canvasFlow;
    private GraphicsContext gc;
    private PixelWriter pw;

    private Boolean isDraggingNode = false;
    private DrawableNode draggedNode = null;
    private Double dragXOffset = 0.0;
    private Double dragYOffset = 0.0;
    private Double scale = 1.0;
    private Double colourCounter = 0.0;
    private Double nodeFontPadding = 15.0; // This is added onto the width of the font
    private Double minimumNodeWidth = 50.0; // Node cannot have a smaller width than this

    public CanvasController(Canvas canvasFlow) {
        this.canvasFlow = canvasFlow;
        gc = canvasFlow.getGraphicsContext2D();
        pw = gc.getPixelWriter();
    }

    public Double getScale() {
        return this.scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public void canvasDragged(MouseEvent event) {
        if (event.isPrimaryButtonDown() && draggedNode != null) {
            draggedNode.setX(event.getX() + dragXOffset);
            draggedNode.setY(event.getY() + dragYOffset);
            drawProgram();
            isDraggingNode = true;
        }
    }

    public void canvasMouseDown(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            Program program = DataBank.currentlyEditProgram;

            if (program != null) {
                List<DrawableNode> clickedNodes = program.getFlowController().getClickedNodes(event.getX(), event.getY());
                if (clickedNodes.size() > 0) {
                    draggedNode = clickedNodes.get(0);
                    dragXOffset = draggedNode.getX() - event.getX();
                    dragYOffset = draggedNode.getY() - event.getY();
                }
            }
        }
    }

    public Boolean canvasMouseUp(MouseEvent event) {
        if (isDraggingNode) {
            DataBank.saveNode(draggedNode);
            draggedNode = null;
            isDraggingNode = false;
            drawProgram();
            return true;
        } else {
            return false;
        }
    }

    public void setFlowNodeScale(DrawableNode flowNode, Double scale) {
//        flowNode.setScale(scale);
//        for (FlowNode loopFlowNode : flowNode.getChildren()) {
//            setFlowNodeScale(loopFlowNode, scale);
//        }
    }

    public void drawProgram() {
        Program program = DataBank.currentlyEditProgram;

        setFlowNodeScale(program.getFlowController().getStartNode(), this.scale);
        gc.clearRect(0, 0, canvasFlow.getWidth(), canvasFlow.getHeight()); // Clears the screen

        // Draw the bottom layer first and build up
        // Connections
        Integer offset = 0;
        for (NodeConnection connection : program.getFlowController().getConnections()) {
            drawConnectingLine(connection.getConnectionStart(), connection.getConnectionEnd(), offset);
            offset++;
        }

        // Nodes
        for (DrawableNode node : program.getFlowController().getNodes()) {
            drawNode(node);
        }
    }

    public void drawNode(DrawableNode drawableNode) {
        gc.setStroke(drawableNode.getColor());
        if (drawableNode instanceof SplitNode) {
            gc.setFill(Color.LIGHTCYAN);
        } else if (drawableNode instanceof FlowNode) {
            gc.setFill(Color.LIGHTGREEN);
        } else if (drawableNode instanceof TestResultNode) {
            gc.setFill(Color.LIGHTSTEELBLUE);
        } else {
            gc.setFill(Color.WHITE);
        }

        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(gc.getFont());
        Float fontWidth = metrics.computeStringWidth(drawableNode.getContainedText());
        if (fontWidth.doubleValue() + nodeFontPadding > minimumNodeWidth) {
            drawableNode.setWidth(fontWidth.doubleValue() + nodeFontPadding);
        } else {
            drawableNode.setWidth(minimumNodeWidth);
        }

        gc.fillRect(drawableNode.getScaledX(), drawableNode.getScaledY(), drawableNode.getScaledWidth(), drawableNode.getScaledHeight());
        gc.strokeRect(drawableNode.getScaledX(), drawableNode.getScaledY(), drawableNode.getScaledWidth(), drawableNode.getScaledHeight());

        gc.setFill(Color.GRAY);
        int offsetThing = 0;
        double innerLoop = 0;
        //colourCounter

//        for (double j = 0; j < 10; j++) {
//            for (innerLoop = colourCounter; innerLoop < 0.7; innerLoop = innerLoop + 0.05) {
//                gc.setFill(new Color(innerLoop, innerLoop, innerLoop, 0.75));
//                gc.fillRect(flowNode.getScaledX() + 20 + offsetThing, flowNode.getScaledY() + 42, 2, 2);
//                offsetThing++;
//            }
//        }
//        colourCounter = colourCounter + 0.05;
//        if (colourCounter > 0.7) {
//            colourCounter = 0.0;
//        }

        drawContainedText(drawableNode);
    }

    public void drawContainedText(DrawableNode drawableNode) {
        if (drawableNode.getContainedText() != null) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.BLACK);
            gc.fillText(drawableNode.getContainedText(), drawableNode.getScaledCenterX(), drawableNode.getScaledCenterY());
        }
    }

    public void drawConnectingLine(DrawableNode startNode, DrawableNode endNode, Integer offset) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        Double startNodePadding = ((startNode.getScaledWidth() / 2) + 10) * startNode.getScale();
        Double endNodePadding = ((endNode.getScaledWidth() / 2) + 10) * endNode.getScale();

        // Center of start to just outside start
        gc.strokeLine(startNode.getScaledCenterX() + startNode.getScaledWidth() / 2, startNode.getScaledCenterY(), startNode.getScaledCenterX() + startNodePadding - 1, startNode.getScaledCenterY());
        // Outside start to corner inline with end
        gc.strokeLine(startNode.getScaledCenterX() + startNodePadding, startNode.getScaledCenterY(), startNode.getScaledCenterX() + startNodePadding, endNode.getScaledCenterY());
        // Corner to just outside end
        gc.strokeLine(startNode.getScaledCenterX() + startNodePadding, endNode.getScaledCenterY(), endNode.getScaledCenterX() - endNodePadding, endNode.getScaledCenterY());
        // Just outside end to center of end
        gc.strokeLine(endNode.getScaledCenterX() - endNodePadding + 1, endNode.getScaledCenterY(), endNode.getScaledCenterX() - endNode.getScaledWidth() / 2, endNode.getScaledCenterY());
    }
}