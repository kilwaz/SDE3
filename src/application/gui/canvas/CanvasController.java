package application.gui.canvas;

import application.data.DataBank;
import application.gui.NodeConnection;
import application.gui.Program;
import application.node.DrawableNode;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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
    private Double nodeFontPadding = 15.0; // This is added onto the width of the font
    private Double minimumNodeWidth = 50.0; // Node cannot have a smaller width than this
    private Integer nodeCornerPadding = 5; // This is the padding space the path network will give around nodes

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
//        for (SourceNode loopFlowNode : flowNode.getChildren()) {
//            setFlowNodeScale(loopFlowNode, scale);
//        }
    }

    public void drawProgram() {
        Program program = DataBank.currentlyEditProgram;

        setFlowNodeScale(program.getFlowController().getStartNode(), this.scale);
        gc.clearRect(0, 0, canvasFlow.getWidth(), canvasFlow.getHeight()); // Clears the screen

        // Draw the bottom layers first and build up
        // Connections
        AStarNetwork network = new AStarNetwork(nodeCornerPadding);
        for (NodeConnection connection : program.getFlowController().getConnections()) {
            gc.setStroke(Color.BLACK);
            List<AStarPoint> solvedPath = network.solvePath(connection);
            AStarPoint currentPoint = network.findStartAStarPointFromNode(connection.getConnectionStart()); // We must get the start after we have solved the path
            for (AStarPoint path : solvedPath) {
                gc.strokeLine(currentPoint.getX(), currentPoint.getY(), path.getX(), path.getY());
                currentPoint = path;
            }
        }

        // Nodes boxes and contained text
        for (DrawableNode node : program.getFlowController().getNodes()) {
            drawNode(node);
        }
    }


    public void drawNode(DrawableNode drawableNode) {
        gc.setStroke(drawableNode.getColor());
        gc.setFont(Font.font("Verdana", 12));
        gc.setLineWidth(1.0);
        gc.setFill(drawableNode.getFillColour());

        List<DrawablePoint> drawablePoints = drawableNode.getDrawablePoints();

        gc.beginPath();
        gc.moveTo(drawableNode.getX(), drawableNode.getY());
        for (DrawablePoint drawablePoint : drawablePoints) {
            if (drawablePoint.isMove()) {
                gc.moveTo(drawablePoint.getX() + drawableNode.getX(), drawablePoint.getY() + drawableNode.getY());
            } else {
                gc.lineTo(drawablePoint.getX() + drawableNode.getX(), drawablePoint.getY() + drawableNode.getY());
            }
        }
        gc.stroke();
        gc.fill();
        gc.closePath();

        gc.setFill(Color.GRAY);
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
}