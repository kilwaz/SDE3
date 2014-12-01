package application.gui.canvas;

import application.data.DataBank;
import application.gui.Controller;
import application.gui.NodeConnection;
import application.gui.Program;
import application.node.DrawableNode;
import application.utils.AppParams;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

import java.util.List;

public class CanvasController {
    private Canvas canvasFlow;
    private GraphicsContext gc;

    private Boolean isDraggingNode = false;
    private Boolean isDraggingCanvas = false;
    private DrawableNode draggedNode = null;
    private Double dragXOffset = 0.0;
    private Double dragYOffset = 0.0;
    private Double scale = 1.0;
    private Double nodeFontPadding = 15.0; // This is added onto the width of the font
    private Double minimumNodeWidth = 50.0; // Node cannot have a smaller width than this
    private Integer nodeCornerPadding = 5; // This is the padding space the path network will give around nodes
    private Double offsetHeight = 0.0;
    private Double offsetWidth = 0.0;
    private Double initialOffsetHeight = 0.0;
    private Double initialOffsetWidth = 0.0;
    private Double initialMouseX = 0.0;
    private Double initialMouseY = 0.0;

    public CanvasController(Canvas canvasFlow) {
        this.canvasFlow = canvasFlow;
        gc = canvasFlow.getGraphicsContext2D();
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
        } else if (event.isPrimaryButtonDown() && isDraggingCanvas) {
            offsetWidth = initialOffsetWidth - (initialMouseX - event.getX());
            offsetHeight = initialOffsetHeight - (initialMouseY - event.getY());
            drawProgram();
        }
    }

    public void canvasMouseDown(MouseEvent event) {
        if (event.isPrimaryButtonDown()) {
            Program program = DataBank.currentlyEditProgram;

            if (program != null) {
                List<DrawableNode> clickedNodes = program.getFlowController().getClickedNodes(event.getX() - offsetWidth, event.getY() - offsetHeight);
                if (clickedNodes.size() > 0) {
                    draggedNode = clickedNodes.get(0);
                    dragXOffset = draggedNode.getX() - event.getX();
                    dragYOffset = draggedNode.getY() - event.getY();
                } else {
                    initialMouseX = event.getX();
                    initialMouseY = event.getY();
                    initialOffsetWidth = offsetWidth;
                    initialOffsetHeight = offsetHeight;
                    Controller.getInstance().setCursor(Cursor.MOVE);
                    isDraggingCanvas = true;
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
        } else if (isDraggingCanvas) {
            isDraggingCanvas = false;
            Controller.getInstance().setCursor(Cursor.DEFAULT);
            return true;
        } else {
            return false;
        }
    }

    public void setFlowNodeScale(DrawableNode flowNode, Double scale) {
//        flowNode.setScale(scale);
//        for (SourceNode loopFlowNode : flowNode.getChildren()) {
//            setFlowNodeScale(loopFlowNode, scale);
//        }
    }

    public void drawProgram() {
        Program program = DataBank.currentlyEditProgram;

        if (program != null) {
            setFlowNodeScale(program.getFlowController().getStartNode(), this.scale);
            gc.clearRect(0, 0, canvasFlow.getWidth(), canvasFlow.getHeight()); // Clears the screen

            // Draw the bottom layers first and build up
            // Connections
            AStarNetwork network = new AStarNetwork(nodeCornerPadding);
            for (NodeConnection connection : program.getFlowController().getConnections()) {
                // Bold lines for the selected node
                if (connection.getConnectionStart() == program.getFlowController().getSelectedNode()
                        || connection.getConnectionEnd() == program.getFlowController().getSelectedNode()) {
                    gc.setLineWidth(2.0);
                } else {
                    gc.setLineWidth(1.0);
                }

                // Get colours for different types of connection
                if (connection.isTriggeredGradient()) {
                    gc.setStroke(connection.getRunGradientColor());
                } else {
                    gc.setStroke(connection.getBaseColor());
                }

                // Solution to path finding
                List<AStarPoint> solvedPath = network.solvePath(connection);
                AStarPoint currentPoint = network.findStartAStarPointFromNode(connection.getConnectionStart()); // We must get the start after we have solved the path
                for (AStarPoint path : solvedPath) {
                    gc.strokeLine(currentPoint.getX() + offsetWidth, currentPoint.getY() + offsetHeight, path.getX() + offsetWidth, path.getY() + offsetHeight);
                    currentPoint = path;
                }

                gc.setLineWidth(1.0);
            }

            // Nodes boxes and contained text
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (node == program.getFlowController().getSelectedNode()) {
                    drawNode(node, true);
                } else {
                    drawNode(node, false);
                }
            }
        }
    }

    public void drawNode(DrawableNode drawableNode, Boolean selected) {
        gc.setStroke(drawableNode.getColor());
        gc.setFont(AppParams.getFont());

        // Highlights the currently selected node with bolder line
        if (selected) {
            gc.setLineWidth(4.0);
        } else {
            gc.setLineWidth(1.0);
        }

        gc.setFill(drawableNode.getFillColour());

        List<DrawablePoint> drawablePoints = drawableNode.getDrawablePoints();

        gc.beginPath();
        gc.moveTo(drawableNode.getX() + offsetWidth, drawableNode.getY() + offsetHeight);
        for (DrawablePoint drawablePoint : drawablePoints) {
            if (drawablePoint.isMove()) {
                gc.moveTo(drawablePoint.getX() + drawableNode.getX() + offsetWidth, drawablePoint.getY() + drawableNode.getY() + offsetHeight);
            } else {
                gc.lineTo(drawablePoint.getX() + drawableNode.getX() + offsetWidth, drawablePoint.getY() + drawableNode.getY() + offsetHeight);
            }
        }
        gc.stroke();
        gc.fill();
        gc.closePath();
        gc.setLineWidth(1.0);

        // Draw the start node arrow
        if (DataBank.currentlyEditProgram.getFlowController().getStartNode().equals(drawableNode)) {
            gc.beginPath();
            gc.moveTo(drawableNode.getX() + offsetWidth - 25, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2));
            gc.lineTo(drawableNode.getX() + offsetWidth - nodeCornerPadding, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2));
            gc.lineTo(drawableNode.getX() + offsetWidth - nodeCornerPadding - 5, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2) - 5);
            gc.moveTo(drawableNode.getX() + offsetWidth - nodeCornerPadding, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2));
            gc.lineTo(drawableNode.getX() + offsetWidth - nodeCornerPadding - 5, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2) + 5);
            gc.stroke();
            gc.closePath();
        }

        gc.setFill(Color.GRAY);
        drawContainedText(drawableNode);
    }

    public void drawContainedText(DrawableNode drawableNode) {
        if (drawableNode.getContainedText() != null) {
            gc.setTextAlign(TextAlignment.CENTER);
            gc.setTextBaseline(VPos.CENTER);
            gc.setFill(Color.BLACK);
            gc.fillText(drawableNode.getContainedText(), drawableNode.getScaledCenterX() + offsetWidth, drawableNode.getScaledCenterY() + offsetHeight);
        }
    }

    public Canvas getCanvasFlow() {
        return canvasFlow;
    }

    public Double getOffsetWidth() {
        return offsetWidth;
    }

    public Double getOffsetHeight() {
        return offsetHeight;
    }
}