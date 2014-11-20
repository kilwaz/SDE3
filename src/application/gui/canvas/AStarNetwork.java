package application.gui.canvas;

import application.data.DataBank;
import application.gui.NodeConnection;
import application.gui.Program;
import application.node.DrawableNode;
import javafx.scene.canvas.GraphicsContext;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AStarNetwork {
    private Integer nodeCornerPadding;
    private GraphicsContext gc;
    private List<AStarPoint> networkPoints;

    public AStarNetwork(Integer nodeCornerPadding) {
        this.nodeCornerPadding = nodeCornerPadding;
        generateAStarNetwork();
    }

    private void generateAStarNetwork() {
        List<Point> points = new ArrayList<Point>();
        Program program = DataBank.currentlyEditProgram;

        // Create points, 6 points one in each corner and then one on the middle left and right edges
        for (DrawableNode node : program.getFlowController().getNodes()) {
            points.add(new Point(node.getX().intValue() - nodeCornerPadding, node.getY().intValue() - nodeCornerPadding)); // Top Left
            points.add(new Point(node.getX().intValue() - nodeCornerPadding, node.getY().intValue() + (node.getHeight().intValue() / 2))); // Middle Left
            points.add(new Point(node.getX().intValue() - nodeCornerPadding, node.getY().intValue() + node.getHeight().intValue() + nodeCornerPadding)); // Bottom Left
            points.add(new Point(node.getX().intValue() + node.getWidth().intValue() + nodeCornerPadding, node.getY().intValue() - nodeCornerPadding)); // Top Right
            points.add(new Point(node.getX().intValue() + node.getWidth().intValue() + nodeCornerPadding, node.getY().intValue() + (node.getHeight().intValue() / 2))); // Middle Right
            points.add(new Point(node.getX().intValue() + node.getWidth().intValue() + nodeCornerPadding, node.getY().intValue() + node.getHeight().intValue() + nodeCornerPadding)); // Bottom Right
        }

        // Draw Points
        //gc.setStroke(javafx.scene.paint.Color.RED);
        //gc.setLineWidth(1);

        networkPoints = new ArrayList<AStarPoint>();
        for (Point point : points) {
            networkPoints.add(new AStarPoint(point.getLocation()));
        }

        for (AStarPoint aStarPointStart : networkPoints) {
            // Debug show network points
            //gc.strokeLine(aStarPointStart.getX(), aStarPointStart.getY(), aStarPointStart.getX(), aStarPointStart.getY());

            for (AStarPoint aStarPointEnd : networkPoints) {
                Boolean intersected = false;
                for (DrawableNode node : program.getFlowController().getNodes()) {
                    Rectangle r1 = new Rectangle(node.getX().intValue() - nodeCornerPadding + 1, node.getY().intValue() - nodeCornerPadding + 1, node.getWidth().intValue() + ((nodeCornerPadding - 1) * 2), node.getHeight().intValue() + ((nodeCornerPadding - 1) * 2));
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
    }

    public List<AStarPoint> solvePath(NodeConnection connection) {
        AStarPoint start = findStartAStarPointFromNode(connection.getConnectionStart());
        AStarPoint goal = findGoalAStarPointFromNode(connection.getConnectionEnd());

        if (start != null && goal != null) {
            return aStarSolve(start, goal);
        } else {
            // Return empty list if no start or goal
            System.out.println("Problems finding start or goal for path");
            return new ArrayList<AStarPoint>();
        }
    }

    public AStarPoint findStartAStarPointFromNode(DrawableNode node) {
        // Find start in network
        AStarPoint foundAStarPoint = null;

        for (AStarPoint aStarPoint : networkPoints) {
            if (node.getX().intValue() + node.getWidth().intValue() + nodeCornerPadding == aStarPoint.getX() &&
                    node.getY().intValue() + (node.getHeight().intValue() / 2) == aStarPoint.getY()) {
                foundAStarPoint = aStarPoint;
            }
        }

        return foundAStarPoint;
    }

    public AStarPoint findGoalAStarPointFromNode(DrawableNode node) {
        // Find start in network
        AStarPoint foundAStarPoint = null;

        for (AStarPoint aStarPoint : networkPoints) {

            if (node.getX().intValue() - nodeCornerPadding == aStarPoint.getX() &&
                    node.getY().intValue() + (node.getHeight().intValue() / 2) == aStarPoint.getY()) {
                foundAStarPoint = aStarPoint;
            }
        }

        return foundAStarPoint;
    }

    private List<AStarPoint> aStarSolve(AStarPoint start, AStarPoint goal) {
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

    public List<AStarPoint> getNetworkPoints() {
        return networkPoints;
    }
}
