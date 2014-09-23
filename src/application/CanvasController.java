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

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
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
    private Integer cornerPadding = 5;

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
        List<AStarPoint> network = generateAStarNetwork();
        for (NodeConnection connection : program.getFlowController().getConnections()) {
            AStarPoint start = null;
            AStarPoint goal = null;

            // Find start in network
            for (AStarPoint aStarPoint : network) {
                DrawableNode startNode = connection.getConnectionStart();

                if (startNode.getX().intValue() + startNode.getWidth().intValue() + cornerPadding == aStarPoint.getX() &&
                        startNode.getY().intValue() + (startNode.getHeight().intValue() / 2) == aStarPoint.getY()) {
                    start = aStarPoint;
                }
            }

            // Find goal in network
            for (AStarPoint aStarPoint : network) {
                DrawableNode endNode = connection.getConnectionEnd();

                if (endNode.getX().intValue() - cornerPadding == aStarPoint.getX() &&
                        endNode.getY().intValue() + (endNode.getHeight().intValue() / 2) == aStarPoint.getY()) {
                    goal = aStarPoint;
                }
            }

            if (start != null && goal != null) {
                List<AStarPoint> solvedPath = aStarSolve(start, goal);
                AStarPoint currentPoint = start;
                gc.setStroke(Color.BLACK);
                for (AStarPoint path : solvedPath) {
                    gc.strokeLine(currentPoint.getX(), currentPoint.getY(), path.getX(), path.getY());
                    currentPoint = path;
                }
            } else {
                System.out.println("Trying to draw connection that doesn't exist!");
            }
        }

        // Nodes boxes and contained text
        for (DrawableNode node : program.getFlowController().getNodes()) {
            drawNode(node);
        }
    }

    public void drawNode(DrawableNode drawableNode) {
        gc.setStroke(drawableNode.getColor());
        if (drawableNode instanceof SplitNode) {
            gc.setFill(Color.LIGHTCYAN);
        } else if (drawableNode instanceof SourceNode) {
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

    public List<AStarPoint> generateAStarNetwork() {
        List<Point> points = new ArrayList<Point>();
        Program program = DataBank.currentlyEditProgram;

        // Create points, 6 points one in each corner and then one on the middle left and right edges
        for (DrawableNode node : program.getFlowController().getNodes()) {
            points.add(new Point(node.getX().intValue() - cornerPadding, node.getY().intValue() - cornerPadding)); // Top Left
            points.add(new Point(node.getX().intValue() - cornerPadding, node.getY().intValue() + (node.getHeight().intValue() / 2))); // Middle Left
            points.add(new Point(node.getX().intValue() - cornerPadding, node.getY().intValue() + node.getHeight().intValue() + cornerPadding)); // Bottom Left
            points.add(new Point(node.getX().intValue() + node.getWidth().intValue() + cornerPadding, node.getY().intValue() - cornerPadding)); // Top Right
            points.add(new Point(node.getX().intValue() + node.getWidth().intValue() + cornerPadding, node.getY().intValue() + (node.getHeight().intValue() / 2))); // Middle Right
            points.add(new Point(node.getX().intValue() + node.getWidth().intValue() + cornerPadding, node.getY().intValue() + node.getHeight().intValue() + cornerPadding)); // Bottom Right
        }

        // Draw Points
        gc.setStroke(Color.RED);
        gc.setLineWidth(1);

        List<AStarPoint> network = new ArrayList<AStarPoint>();
        for (Point point : points) {
            network.add(new AStarPoint(point.getLocation()));
        }

        for (AStarPoint aStarPointStart : network) {
            // Debug show network points
            //gc.strokeLine(aStarPointStart.getX(), aStarPointStart.getY(), aStarPointStart.getX(), aStarPointStart.getY());

            for (AStarPoint aStarPointEnd : network) {
                Boolean intersected = false;
                for (DrawableNode node : program.getFlowController().getNodes()) {
                    Rectangle r1 = new Rectangle(node.getX().intValue() - cornerPadding + 1, node.getY().intValue() - cornerPadding + 1, node.getWidth().intValue() + ((cornerPadding - 1) * 2), node.getHeight().intValue() + ((cornerPadding - 1) * 2));
                    Line2D l1 = new Line2D.Double(aStarPointStart.getX(), aStarPointStart.getY(), aStarPointEnd.getX(), aStarPointEnd.getY());
                    if (l1.intersects(r1)) {
                        intersected = true;
                        break;
                    }
                }

                if (!intersected) {
                    // Debug show network connecting lines
                    //gc.strokeLine(aStarPointStart.getX(), aStarPointStart.getY(), aStarPointEnd.getX(), aStarPointEnd.getY());
                    aStarPointStart.addConnection(aStarPointEnd);
                    aStarPointEnd.addConnection(aStarPointStart);
                }
            }
        }

        return network;
    }

    public List<AStarPoint> aStarSolve(AStarPoint start, AStarPoint goal) {
        List<AStarPoint> solvedPath = new ArrayList<AStarPoint>();
        List<AStarPoint> closedSet = new ArrayList<AStarPoint>();
        List<AStarPoint> openSet = new ArrayList<AStarPoint>();
        openSet.add(start);

        // Works as second point back to the first
        HashMap<AStarPoint, AStarPoint> routes = new HashMap<AStarPoint, AStarPoint>();

        HashMap<AStarPoint, Double> knownScore = new HashMap<AStarPoint, Double>();
        HashMap<AStarPoint, Double> estimatedScore = new HashMap<AStarPoint, Double>();
        knownScore.put(start, 0.0);

        // Calculate the estimated distance by first going along known and the estimating the rest
        estimatedScore.put(start, knownScore.get(start) + start.getStraightLineDistance(goal));

        // Keep going while there are points left to search
        while (openSet.size() > 0) {
            // Get the first point initially as 'best'
            AStarPoint currentLowestScore = openSet.get(0);

            // Find the lowest score point to search first
            for (AStarPoint openSetPoint : openSet) {
                if (estimatedScore.get(openSetPoint) < estimatedScore.get(currentLowestScore)) {
                    currentLowestScore = openSetPoint;
                }
            }

            // If the lowest score is the goal, we have completed the path
            if (currentLowestScore.equals(goal)) {
                return constructPath(routes, goal);
            }

            openSet.remove(currentLowestScore);
            closedSet.add(currentLowestScore);

            // Loop through the connected points to this one
            for (AStarPoint connected : currentLowestScore.getConnections()) {
                // If the point is closed continue on to the next one
                if (closedSet.contains(connected)) {
                    continue;
                }

                Double tentativeKnownScore = knownScore.get(currentLowestScore) + currentLowestScore.getStraightLineDistance(connected);
                if (knownScore.get(connected) == null) {
                    knownScore.put(connected, Double.MAX_VALUE);
                }
                if (!openSet.contains(connected) && tentativeKnownScore < knownScore.get(connected)) {
                    routes.put(connected, currentLowestScore);
                    knownScore.put(connected, tentativeKnownScore);
                    estimatedScore.put(connected, tentativeKnownScore + connected.getStraightLineDistance(goal));

                    if (!openSet.contains(connected)) {
                        openSet.add(connected);
                    }
                }
            }
        }

        return solvedPath;
    }

    public List<AStarPoint> constructPath(HashMap<AStarPoint, AStarPoint> routes, AStarPoint currentPoint) {
        if (routes.containsKey(currentPoint)) {
            List<AStarPoint> path = constructPath(routes, routes.get(currentPoint));
            path.add(currentPoint);

            return path;
        } else {
            List<AStarPoint> path = new ArrayList<AStarPoint>();
            path.add(currentPoint);

            return path;
        }
    }
}