package sde.application.gui.canvas;

import sde.application.error.Error;
import sde.application.gui.NodeConnection;
import sde.application.gui.Program;
import sde.application.node.design.DrawableNode;
import sde.application.utils.managers.SessionManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class AStarNetwork {
    private Integer nodeCornerPadding;
    //    private GraphicsContext gc;   // Used here for drawing debug view of network
    private List<AStarPoint> networkPoints;
    private static Logger log = Logger.getLogger(AStarNetwork.class);

    public AStarNetwork(Integer nodeCornerPadding) {
        Program program = SessionManager.getInstance().getCurrentSession().getSelectedProgram();
        this.nodeCornerPadding = nodeCornerPadding;
        updateAStarNetwork(program.getFlowController().getNodes(), true);
    }

    public void updateAStarNetwork(List<DrawableNode> nodes, Boolean recalculateVisibility) {
        List<Point> points = new ArrayList<>();
        Program program = SessionManager.getInstance().getCurrentSession().getSelectedProgram();

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

        networkPoints = new ArrayList<>();
        networkPoints.addAll(points.stream().map(point -> new AStarPoint(point.getLocation())).collect(Collectors.toList()));

        for (AStarPoint aStarPointStart : networkPoints) {
            // Debug show network points
            //gc.strokeLine(aStarPointStart.getX(), aStarPointStart.getY(), aStarPointStart.getX(), aStarPointStart.getY());

            for (AStarPoint aStarPointEnd : networkPoints) {
                Boolean intersected = false;
                // Seeing as calculating visibility of other nodes is expensive we only do this when the user releases the mouse, gives a smoother experience
                if (recalculateVisibility) {
                    for (DrawableNode node : nodes) {
                        Rectangle r1 = new Rectangle(node.getX().intValue() - nodeCornerPadding + 1, node.getY().intValue() - nodeCornerPadding + 1, node.getWidth().intValue() + ((nodeCornerPadding - 1) * 2), node.getHeight().intValue() + ((nodeCornerPadding - 1) * 2));
                        Line2D l1 = new Line2D.Double(aStarPointStart.getX(), aStarPointStart.getY(), aStarPointEnd.getX(), aStarPointEnd.getY());
                        if (l1.intersects(r1)) {
                            intersected = true;
                            break; // Break as soon as we find a node which intersects as we then know it is not valid
                        }
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
            Error.A_STAR_START_GOAL_MISSING.record().create();
            return new ArrayList<>();
        }
    }

    public AStarPoint findStartAStarPointFromNode(DrawableNode node) {
        // Find start in network
        AStarPoint foundAStarPoint = null;

        for (AStarPoint aStarPoint : networkPoints) {
            if (node.getX().intValue() + node.getWidth().intValue() + nodeCornerPadding == aStarPoint.getX() &&
                    node.getY().intValue() + (node.getHeight().intValue() / 2) == aStarPoint.getY()) {
                foundAStarPoint = aStarPoint;
                break;
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
                break;
            }
        }

        return foundAStarPoint;
    }

    private List<AStarPoint> aStarSolve(AStarPoint start, AStarPoint goal) {
        List<AStarPoint> solvedPath = new ArrayList<>();
        List<AStarPoint> closedSet = new ArrayList<>();
        List<AStarPoint> openSet = new ArrayList<>();
        openSet.add(start);

        // Works as second point back to the first
        HashMap<AStarPoint, AStarPoint> routes = new HashMap<>();

        HashMap<AStarPoint, Double> knownScore = new HashMap<>();
        HashMap<AStarPoint, Double> estimatedScore = new HashMap<>();
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
            List<AStarPoint> path = new ArrayList<>();
            path.add(currentPoint);

            return path;
        }
    }

    public List<AStarPoint> getNetworkPoints() {
        return networkPoints;
    }
}
