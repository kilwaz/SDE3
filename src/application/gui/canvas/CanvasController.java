package application.gui.canvas;

import application.data.DataBank;
import application.gui.Controller;
import application.gui.FlowController;
import application.gui.NodeConnection;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.node.implementations.LinuxNode;
import application.utils.AppParams;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CanvasController {
    private Canvas canvasFlow;
    private GraphicsContext gc;

    private Boolean isDraggingNode = false;
    private Boolean isDraggingCanvas = false;
    private Boolean isGroupSelect = false;
    private List<DrawableNode> draggedNodes = new ArrayList<>();
    private HashMap<DrawableNode, List<Double>> draggedOffsets = new HashMap<>();
    private Double dragXOffset = 0.0;
    private Double dragYOffset = 0.0;
    private Double scale = 1.0;
    private Integer nodeCornerPadding = 5; // This is the padding space the path network will give around nodes
    private Double offsetHeight = 0.0;
    private Double offsetWidth = 0.0;

    private Double initialOffsetHeight = 0.0;
    private Double initialOffsetWidth = 0.0;
    private Double initialMouseX = 0.0;
    private Double initialMouseY = 0.0;

    private Double groupSelectInitialX = 0.0;
    private Double groupSelectInitialY = 0.0;
    private Double groupSelectCurrentX = 0.0;
    private Double groupSelectCurrentY = 0.0;

    private HashMap<NodeConnection, List<AStarPoint>> solvedPathsCache = new HashMap<>();
    private HashMap<NodeConnection, AStarPoint> startPointsCache = new HashMap<>();
    private AStarNetwork network = null;

    private static Logger log = Logger.getLogger(CanvasController.class);

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
        if (isGroupSelect) {
            groupSelectCurrentX = event.getX();
            groupSelectCurrentY = event.getY();
            updateAStarNetwork(draggedNodes);
            drawProgram();  // Draw program method will draw the selection box as it is being dragged
        } else {
            if (event.isPrimaryButtonDown() && draggedNodes.size() > 0) {
                for (DrawableNode draggedNode : draggedNodes) {
                    List<Double> dragOffsetXY = draggedOffsets.get(draggedNode);
                    draggedNode.setX(event.getX() + dragOffsetXY.get(0));
                    draggedNode.setY(event.getY() + dragOffsetXY.get(1));
                }
                updateAStarNetwork(draggedNodes);
                drawProgram();
                isDraggingNode = true;
            } else if (event.isPrimaryButtonDown() && isDraggingCanvas) {
                offsetWidth = initialOffsetWidth - (initialMouseX - event.getX());
                offsetHeight = initialOffsetHeight - (initialMouseY - event.getY());
                DataBank.currentlyEditProgram.getFlowController().setViewOffsetWidth(offsetWidth);
                DataBank.currentlyEditProgram.getFlowController().setViewOffsetHeight(offsetHeight);
                drawProgram();
            }
        }
    }

    public void canvasMouseDown(MouseEvent event) {
        if (event.isControlDown()) { // This is when we are holding control to select a group of nodes
            isGroupSelect = true;
            groupSelectInitialX = event.getX();
            groupSelectInitialY = event.getY();
        } else {
            if (event.isPrimaryButtonDown()) {
                Program program = DataBank.currentlyEditProgram;

                if (program != null) {
                    List<DrawableNode> selectedNodes = program.getFlowController().getSelectedNodes();
                    if (selectedNodes.size() == 0 || selectedNodes.size() == 1) {
                        selectedNodes = program.getFlowController().getClickedNodes(event.getX() - offsetWidth, event.getY() - offsetHeight);
                        program.getFlowController().setSelectedNodes(selectedNodes);
                    }
                    draggedNodes = selectedNodes;

                    if (selectedNodes.size() > 0) {
                        draggedOffsets.clear();
                        for (DrawableNode draggedNode : draggedNodes) {
                            List<Double> dragOffsetXY = new ArrayList<>();
                            dragOffsetXY.add(draggedNode.getX() - event.getX());
                            dragOffsetXY.add(draggedNode.getY() - event.getY());
                            draggedOffsets.put(draggedNode, dragOffsetXY);
                        }
                    } else {
                        initialMouseX = event.getX();
                        initialMouseY = event.getY();
                        initialOffsetWidth = offsetWidth;
                        initialOffsetHeight = offsetHeight;
                        Controller.getInstance().setCursor(Cursor.MOVE);
                        isDraggingCanvas = true;
                        draggedNodes = new ArrayList<>();
                        isDraggingNode = false;
                    }
                }
            }
        }
    }

    public Boolean canvasMouseUp(MouseEvent event) {
        if (isDraggingNode) {
            draggedNodes.forEach(DataBank::saveNode);
            draggedNodes = new ArrayList<>();
            isDraggingNode = false;
            updateAStarNetwork();
            drawProgram();
            return true;
        } else if (isDraggingCanvas) {
            isDraggingCanvas = false;
            Controller.getInstance().setCursor(Cursor.DEFAULT);
            DataBank.saveProgram(DataBank.currentlyEditProgram);
            return true;
        } else if (isGroupSelect) {
            isGroupSelect = false;
            Program program = DataBank.currentlyEditProgram;
            if (program != null) {
                List<DrawableNode> selectedNodes = program.getFlowController().getGroupSelectedNodes(groupSelectInitialX - offsetWidth, groupSelectInitialY - offsetHeight, event.getX() - offsetWidth, event.getY() - offsetHeight);
                program.getFlowController().setSelectedNodes(selectedNodes);
                updateAStarNetwork();
            }
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

    public void updateAStarNetwork() {
        Program program = DataBank.currentlyEditProgram;
        network = null;
        if (program != null) {
            updateAStarNetwork(program.getFlowController().getNodes());
        }
    }

    public void updateAStarNetwork(List<DrawableNode> nodesToUpdate) {
        Program program = DataBank.currentlyEditProgram;

        FlowController flowController = program.getFlowController();
        List<NodeConnection> connectionsToUpdate = new ArrayList<>();

        for (DrawableNode node : nodesToUpdate) {
            connectionsToUpdate.addAll(flowController.getConnections(node));
        }

        if (network == null) {
            network = new AStarNetwork(nodeCornerPadding);
            solvedPathsCache.clear();
            startPointsCache.clear();
        } else {
            network.updateAStarNetwork(nodesToUpdate, false);
        }

        for (NodeConnection connection : connectionsToUpdate) {
            // Solution to path finding for each connection, these are cached and only recalculated when needed
            solvedPathsCache.put(connection, network.solvePath(connection));
            startPointsCache.put(connection, network.findStartAStarPointFromNode(connection.getConnectionStart())); // We must get the start after we have solved the path
        }
    }

    public void drawProgram() {
        Program program = DataBank.currentlyEditProgram;

        if (program != null) {
            offsetWidth = program.getFlowController().getViewOffsetWidth();
            offsetHeight = program.getFlowController().getViewOffsetHeight();

            setFlowNodeScale(program.getFlowController().getStartNode(), this.scale);
            gc.clearRect(0, 0, canvasFlow.getWidth(), canvasFlow.getHeight()); // Clears the screen

            // Draw the bottom layers first and build up
            for (NodeConnection connection : program.getFlowController().getConnections()) {
                // Bold lines for the selected node
                if (program.getFlowController().getSelectedNodes().contains(connection.getConnectionStart())
                        || program.getFlowController().getSelectedNodes().contains(connection.getConnectionEnd())) {
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

                // If we are missing the routes for these connections recalculate the network
                if (solvedPathsCache.get(connection) == null || startPointsCache.get(connection) == null) {
                    updateAStarNetwork();
                }

                // Refer to the network cache to get the paths
                List<AStarPoint> currentSolvedPath = solvedPathsCache.get(connection);
                AStarPoint currentPoint = startPointsCache.get(connection); // We must get the start after we have solved the path
                for (AStarPoint path : currentSolvedPath) {
                    gc.strokeLine(currentPoint.getX() + offsetWidth, currentPoint.getY() + offsetHeight, path.getX() + offsetWidth, path.getY() + offsetHeight);
                    currentPoint = path;
                }

                gc.setLineWidth(1.0);
            }

            // Nodes boxes and contained text
            for (DrawableNode node : program.getFlowController().getNodes()) {
                if (program.getFlowController().getSelectedNodes().contains(node)) {
                    drawNode(node, true);
                } else {
                    drawNode(node, false);
                }
            }

            // Draws the box if required to show selected bounding box while using is pressing control and dragging
            if (isGroupSelect) {
                gc.setLineWidth(1.0);
                gc.setStroke(Color.BLACK);
                gc.strokeRect(groupSelectInitialX, groupSelectInitialY, groupSelectCurrentX - groupSelectInitialX, groupSelectCurrentY - groupSelectInitialY);
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
        DrawableNode startNode = DataBank.currentlyEditProgram.getFlowController().getStartNode();
        if (startNode != null && startNode.equals(drawableNode)) {
            gc.beginPath();
            gc.moveTo(drawableNode.getX() + offsetWidth - 25, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2));
            gc.lineTo(drawableNode.getX() + offsetWidth - nodeCornerPadding, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2));
            gc.lineTo(drawableNode.getX() + offsetWidth - nodeCornerPadding - 5, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2) - 5);
            gc.moveTo(drawableNode.getX() + offsetWidth - nodeCornerPadding, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2));
            gc.lineTo(drawableNode.getX() + offsetWidth - nodeCornerPadding - 5, drawableNode.getY() + offsetHeight + (drawableNode.getHeight() / 2) + 5);
            gc.stroke();
            gc.closePath();
        }

        // Draws the connected status of a LinuxNode
        if (drawableNode instanceof LinuxNode) {
            if (((LinuxNode) drawableNode).isConnected()) {
                gc.beginPath();
                gc.setFill(Color.LIGHTGREEN);
                gc.moveTo(drawableNode.getX() + offsetWidth + 4, drawableNode.getY() + offsetHeight + 4);
                gc.lineTo(drawableNode.getX() + offsetWidth + 8, drawableNode.getY() + offsetHeight + 4);
                gc.lineTo(drawableNode.getX() + offsetWidth + 8, drawableNode.getY() + offsetHeight + 8);
                gc.lineTo(drawableNode.getX() + offsetWidth + 4, drawableNode.getY() + offsetHeight + 8);
                gc.lineTo(drawableNode.getX() + offsetWidth + 4, drawableNode.getY() + offsetHeight + 4);
                gc.fill();
                gc.closePath();
            }
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