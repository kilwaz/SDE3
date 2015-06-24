package application.node.design;

import application.data.DataBank;
import application.data.SavableAttribute;
import application.gui.canvas.DrawablePoint;
import application.node.objects.Trigger;
import application.utils.AppParams;
import application.utils.NodeRunParams;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
    private String nextNodeToRun = "";
    private Boolean initialising = false;
    private List<Trigger> listeners = new ArrayList<>();

    public static final List<String> NODE_NAMES = new ArrayList<>();

    static {
        // This section of code finds all of the node classes apart from DrawableNode and collects the names as a lookup reference.

        //Reflections reflections = new Reflections("application.node.implementations");
        //reflections.getTypesAnnotatedWith()

        // TEMP FIX UNTIL I CAN GET THIS WORKING!!
        NODE_NAMES.add("BashNode");
        NODE_NAMES.add("BatchNode");
        NODE_NAMES.add("ChartNode");
        NODE_NAMES.add("ConsoleNode");
        NODE_NAMES.add("CopyNode");
        NODE_NAMES.add("CustomObjectNode");
        NODE_NAMES.add("DataBaseNode");
        NODE_NAMES.add("EmailNode");
        NODE_NAMES.add("ExportNode");
        NODE_NAMES.add("InputNode");
        NODE_NAMES.add("LinuxNode");
        NODE_NAMES.add("LogicNode");
        NODE_NAMES.add("RequestTrackerNode");
        NODE_NAMES.add("SwitchNode");
        NODE_NAMES.add("TestNode");
        NODE_NAMES.add("TestResultNode");
        NODE_NAMES.add("TimerNode");
        NODE_NAMES.add("TriggerNode");
        NODE_NAMES.add("WindowsNode");
        // !!!!!!!!!!!!!!!!!!


//        List<Class<?>> classes = ClassFinder.find("application.node.implementations");
//
//        for (Class clazz : classes) {
//            String simpleClassName = clazz.getSimpleName();
//
//            if (simpleClassName.endsWith("Node")) {
//                NODE_NAMES.add(clazz.getSimpleName());
//                Collections.sort(NODE_NAMES);
//            }
//        }
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
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("X", x.getClass().getName(), x));
        savableAttributes.add(new SavableAttribute("Id", id.getClass().getName(), id));
        savableAttributes.add(new SavableAttribute("Y", y.getClass().getName(), y));
        savableAttributes.add(new SavableAttribute("ContainedText", containedText.getClass().getName(), containedText));
        savableAttributes.add(new SavableAttribute("NextNodeToRun", nextNodeToRun.getClass().getName(), nextNodeToRun));

        return savableAttributes;
    }

    public List<DrawablePoint> getDrawablePoints() {
        List<DrawablePoint> drawablePoints = new ArrayList<>();

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

    public Document getXMLRepresentation() {
        Document document;
        Element elementNode = null;

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            document = db.newDocument();

            // create the root element
            Element nodeElement = document.createElement(this.getClass().getSimpleName());

            // Loops through savable attributes
            for (SavableAttribute savableAttribute : getDataToSave()) {
                elementNode = document.createElement("Variable");

                Element className = document.createElement("ClassName");
                className.appendChild(document.createTextNode(savableAttribute.getClassName()));

                Element variableName = document.createElement("VariableName");
                variableName.appendChild(document.createTextNode(savableAttribute.getVariableName()));

                Element variableValue = document.createElement("VariableValue");
                variableValue.appendChild(document.createCDATASection(savableAttribute.getVariable().toString()));

                elementNode.appendChild(variableName);
                elementNode.appendChild(className);
                elementNode.appendChild(variableValue);

                nodeElement.appendChild(elementNode);
            }

            document.appendChild(nodeElement);

            return document;
        } catch (ParserConfigurationException ex) {
            System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + ex);
        }

        return null;
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
        return DataBank.getNodeColours().getNodeColour(getNodeType()).getColour();
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

    public void setAceTextAreaText(String logic) {
    }

    public List<String> getAvailableTriggers() {
        return new ArrayList<>();
    }

    public List<String> getAvailableTriggerActions() {
        return new ArrayList<>();
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
    }

    public Boolean isCoordinateInside(Double x, Double y) {
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
