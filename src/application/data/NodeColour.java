package application.data;

import javafx.scene.paint.Color;

public class NodeColour {
    private Integer id;
    private Integer red;
    private Integer green;
    private Integer blue;
    private String nodeType;

    public NodeColour(Color color, String nodeType) {
        this.red = new Double(color.getRed() * 255).intValue();
        this.green = new Double(color.getGreen() * 255).intValue();
        this.blue = new Double(color.getBlue() * 255).intValue();
        this.nodeType = nodeType;
    }

    public NodeColour(Integer red, Integer green, Integer blue, String nodeType) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.nodeType = nodeType;
    }

    public Integer getRed() {
        return red;
    }

    public void setRed(Integer red) {
        this.red = red;
    }

    public Integer getGreen() {
        return green;
    }

    public void setGreen(Integer green) {
        this.green = green;
    }

    public Integer getBlue() {
        return blue;
    }

    public void setBlue(Integer blue) {
        this.blue = blue;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public Color getColour() {
        return Color.rgb(red, green, blue);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
