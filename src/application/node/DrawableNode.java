package application.node;

import application.data.SavableAttribute;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class DrawableNode {
    private Integer id = -1;
    private Double x = 0.0;
    private Double y = 0.0;
    private Double width = 40.0;
    private Double height = 40.0;
    private Color color = Color.BLACK;
    private Double scale = 1.0;
    private String containedText = "Unnamed";
    private Integer programId = -1;

    public DrawableNode(Integer id, Integer programId) {
        this.programId = programId;
        this.id = id;
    }

    public DrawableNode(Double x, Double y, Double width, Double height, Color color, String containedText, Integer programId, Integer id) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.color = color;
        this.containedText = containedText;
        this.programId = programId;
        this.id = id;
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<SavableAttribute>();

        savableAttributes.add(new SavableAttribute("X", x.getClass().getName(), x));
        savableAttributes.add(new SavableAttribute("Id", id.getClass().getName(), id));
        savableAttributes.add(new SavableAttribute("Y", y.getClass().getName(), y));
        savableAttributes.add(new SavableAttribute("ContainedText", containedText.getClass().getName(), containedText));

        return savableAttributes;
    }

    public Double getCenterX() {
        return this.x + (this.width / 2);
    }

    public Double getCenterY() {
        return this.y + (this.height / 2);
    }

    public Double getX() {
        return this.x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return this.y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getWidth() {
        return this.width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return this.height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }

    public Double getScaledCenterX() {
        return (this.x * scale) + ((this.width * scale) / 2);
    }

    public Double getScaledCenterY() {
        return (this.y * scale) + ((this.height * scale) / 2);
    }

    public Double getScaledX() {
        return this.x * scale;
    }

    public Double getScaledY() {
        return this.y * scale;
    }

    public Double getScaledWidth() {
        return this.width * scale;
    }

    public Double getScaledHeight() {
        return this.height * scale;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getContainedText() {
        return containedText;
    }

    public Double getScale() {
        return this.scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

    public void setContainedText(String containedText) {
        this.containedText = containedText;
    }

    public Integer getProgramId() {
        return this.programId;
    }

    public void setProgramId(Integer programId) {
        this.programId = programId;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNodeType() {
        return "DrawableNode";
    }

    public Boolean isCoordInside(Double x, Double y) {
        if (x > this.x * scale && x < this.x * scale + this.width * scale) {
            if (y > this.y * scale && y < this.y * scale + this.height * scale) {
                return true;
            }
        }

        return false;
    }
}
