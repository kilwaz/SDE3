package application.node;

import application.data.SavableAttribute;
import application.gui.canvas.DrawablePoint;
import application.utils.AppParams;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashMap;
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
    private String nextNodeToRun = "";
    private Boolean initialising = false;
    private Color fillColour = Color.WHITE;

    public static final List<String> NODE_NAMES = new ArrayList<>();

    static {
        NODE_NAMES.add("BashNode");
        NODE_NAMES.add("ConsoleNode");
        NODE_NAMES.add("InputNode");
        NODE_NAMES.add("LinuxNode");
        NODE_NAMES.add("SourceNode");
        NODE_NAMES.add("SwitchNode");
        NODE_NAMES.add("TestResultNode");
        NODE_NAMES.add("TimerNode");
    }

    public DrawableNode() {
    }

    public DrawableNode(DrawableNode drawableNode) {
    }

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
        savableAttributes.add(new SavableAttribute("NextNodeToRun", nextNodeToRun.getClass().getName(), nextNodeToRun));

        return savableAttributes;
    }

    public List<DrawablePoint> getDrawablePoints() {
        List<DrawablePoint> drawablePoints = new ArrayList<DrawablePoint>();

        Double minimumNodeWidth = 50.0;
        Double nodeFontPadding = 5.0;

        FontMetrics metrics = Toolkit.getToolkit().getFontLoader().getFontMetrics(AppParams.getFont());
        Float fontWidth = metrics.computeStringWidth(getContainedText());
        if (fontWidth.doubleValue() + nodeFontPadding > minimumNodeWidth) {
            setWidth(fontWidth.doubleValue() + nodeFontPadding);
        } else {
            setWidth(minimumNodeWidth);
        }

        drawablePoints.add(new DrawablePoint(0.0, 0.0, false));
        drawablePoints.add(new DrawablePoint(getWidth(), 0.0, false));
        drawablePoints.add(new DrawablePoint(getWidth(), getHeight(), false));
        drawablePoints.add(new DrawablePoint(0.0, getHeight(), false));
        drawablePoints.add(new DrawablePoint(0.0, 0.0, false));

        return drawablePoints;
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

    public Tab createInterface() {
        return new Tab();
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

    public Color getFillColour() {
        return this.fillColour;
    }

    public void setFillColour(Color fillColour) {
        this.fillColour = fillColour;
    }

    public String getNodeType() {
        return this.getClass().getSimpleName();
    }

    public String getNextNodeToRun() {
        return nextNodeToRun;
    }

    public void setNextNodeToRun(String nextNodeToRun) {
        this.nextNodeToRun = nextNodeToRun;
    }

    public String getAceTextAreaText() {
        return "";
    }

    public void setAceTextAreaText(String source) {
    }

    public void run(Boolean whileWaiting, HashMap<String, Object> map) {
    }

    public Boolean isCoordInside(Double x, Double y) {
        if (x > this.x * scale && x < this.x * scale + this.width * scale) {
            if (y > this.y * scale && y < this.y * scale + this.height * scale) {
                return true;
            }
        }

        return false;
    }

    public Boolean isInitialising() {
        return initialising;
    }

    public void setIsInitialising(Boolean initialising) {
        this.initialising = initialising;
    }

    public String toString() {
        return this.containedText;
    }
}
