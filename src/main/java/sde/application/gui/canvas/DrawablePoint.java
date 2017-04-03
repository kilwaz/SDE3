package sde.application.gui.canvas;

public class DrawablePoint {
    private Double x;
    private Double y;
    private Boolean isMove = false;

    public DrawablePoint(Double x, Double y, Boolean isMove) {
        this.x = x;
        this.y = y;
        this.isMove = isMove;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Boolean isMove() {
        return isMove;
    }

    public void setIsMove(Boolean isMove) {
        this.isMove = isMove;
    }
}
