package application;

import java.awt.*;
import java.util.ArrayList;

public class AStarPoint {
    private ArrayList<AStarPoint> connections = new ArrayList<AStarPoint>();
    private Point point;

    public AStarPoint(Point point) {
        this.point = point;
    }

    public AStarPoint(Integer x, Integer y) {
        this.point = new Point(x, y);
    }

    public Point getPoint() {
        return point;
    }

    public Integer getX() {
        return ((Double) point.getX()).intValue();
    }

    public Integer getY() {
        return ((Double) point.getY()).intValue();
    }

    public void addConnection(AStarPoint aStarPoint) {
        if (!connections.contains(aStarPoint)) {
            connections.add(aStarPoint);
        }
    }

    public ArrayList<AStarPoint> getConnections() {
        return connections;
    }

    public Double getStraightLineDistance(AStarPoint aStarPoint) {
        // Pythagoras
        Double a = (double) Math.abs(getX() - aStarPoint.getX());
        Double b = (double) Math.abs(getY() - aStarPoint.getY());

        return Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
    }
}
