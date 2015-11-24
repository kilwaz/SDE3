package application.node.design;

import application.Main;
import application.data.DataBank;
import application.data.SavableAttribute;
import application.data.model.DatabaseObject;
import application.data.model.dao.SavableAttributeDAO;
import application.error.Error;
import application.gui.Program;
import application.gui.canvas.DrawablePoint;
import application.node.objects.Trigger;
import application.utils.AppParams;
import application.utils.ClassFinder;
import application.utils.NodeRunParams;
import application.utils.SDEUtils;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Alex Brown
 */

public class DrawableNode extends DatabaseObject {
    private Double x = 0.0;
    private Double y = 0.0;
    private Double width = 40.0;
    private Double height = 40.0;
    private Color color = Color.BLACK;
    private Double scale = 1.0;
    private String containedText = "Unnamed";
    private Program program;
    private String nextNodeToRun = "";
    private Boolean initialising = false;
    private List<Trigger> listeners = new ArrayList<>();

    private static final List<String> NODE_NAMES = new ArrayList<>();
    private static Logger log = Logger.getLogger(DrawableNode.class);

    public static List<String> getNodeNames() {
        return NODE_NAMES;
    }

    static {
        // This section of code finds all of the node classes apart from DrawableNode and collects the names as a lookup reference.
        String path = SDEUtils.getNodeImplementationsClassPath();
        List<Class<?>> classes = ClassFinder.find(new File(path), "application.node.implementations");

        // Used for finding node class name when we are running from an exploded jar
        for (Class clazz : classes) {
            String simpleClassName = clazz.getSimpleName();

            if (simpleClassName.endsWith("Node")) {
                NODE_NAMES.add(clazz.getSimpleName());
                Collections.sort(NODE_NAMES);
            }
        }

        // Used for finding node class names when running inside a jar
        ZipInputStream zip = null;
        try {
            CodeSource src = Main.class.getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                zip = new ZipInputStream(jar.openStream());
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;
                    String name = e.getName();
                    if (name.startsWith("application/node/implementation") && name.contains("Node.class")) {
                        String className = name.substring(name.lastIndexOf("/") + 1, name.indexOf("."));
                        NODE_NAMES.add(className);
                        Collections.sort(NODE_NAMES);
                    }
                }
            }
        } catch (IOException ex) {
            Error.PARSING_NODE_CLASS.record().create(ex);
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException ex) {
                    Error.CLOSE_ZIP.record().create(ex);
                }
            }
        }
    }

    public DrawableNode() {
    }

    public DrawableNode(DrawableNode drawableNode) {
    }

    public DrawableNode(UUID uuid, UUID programUuid) {
        super(uuid);
        //this.programUuid = programUuid;
    }

    public DrawableNode(Double x, Double y, Double width, Double height, Color color, String containedText, UUID programUuid, UUID uuid) {
        super();
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        this.color = color;
        this.containedText = containedText;
        //this.programUuid = programUuid;
    }

    public void save() {
        super.save();
        getDataToSave().forEach(application.data.SavableAttribute::save);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        // X
        SavableAttribute xAttribute = SavableAttribute.create(SavableAttribute.class);
        xAttribute.init("X", x.getClass().getName(), x, this);
        savableAttributes.add(xAttribute);

        // Y
        SavableAttribute yAttribute = SavableAttribute.create(SavableAttribute.class);
        yAttribute.init("Y", y.getClass().getName(), y, this);
        savableAttributes.add(yAttribute);

        // ContainedText
        SavableAttribute containedTextAttribute = SavableAttribute.create(SavableAttribute.class);
        containedTextAttribute.init("ContainedText", containedText.getClass().getName(), containedText, this);
        savableAttributes.add(containedTextAttribute);

        // NextNodeToRun
        SavableAttribute nextNodeToRunAttribute = SavableAttribute.create(SavableAttribute.class);
        nextNodeToRunAttribute.init("NextNodeToRun", nextNodeToRun.getClass().getName(), nextNodeToRun, this);
        savableAttributes.add(nextNodeToRunAttribute);

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

        // instance of a DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use factory to get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // create instance of DOM
            document = db.newDocument();

            document.appendChild(getXMLRepresentation(document));

            return document;
        } catch (ParserConfigurationException ex) {
            Error.NODE_XML_REPRESENTATION.record().create(ex);
        }

        return null;
    }

    public void delete() {
        SavableAttributeDAO savableAttributeDAO = new SavableAttributeDAO();
        List<SavableAttribute> savableAttributes = savableAttributeDAO.getAttributes(this);
        savableAttributes.forEach(application.data.SavableAttribute::delete);
        super.delete();
    }

    public Element getXMLRepresentation(Document document) {
        Element elementNode = null;
        // Create the root element
        Element nodeElement = document.createElement(this.getClass().getSimpleName());

        // Record if this node is the start node of the program
        DrawableNode drawableNode = program.getFlowController().getStartNode();
        if (drawableNode != null && drawableNode.equals(this)) {
            Element startNodeElement = document.createElement("IsStartNode");
            startNodeElement.appendChild(document.createTextNode("Yes"));
            nodeElement.appendChild(startNodeElement);
        }

        // Loops through savable attributes
        for (SavableAttribute savableAttribute : getDataToSave()) {
            elementNode = document.createElement("Variable");

            Element className = document.createElement("ClassName");
            className.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(savableAttribute.getClassName())));

            Element variableName = document.createElement("VariableName");
            variableName.appendChild(document.createTextNode(SDEUtils.escapeXMLCData(savableAttribute.getVariableName())));

            Element variableValue = document.createElement("VariableValue");
            variableValue.appendChild(document.createCDATASection(SDEUtils.escapeXMLCData(savableAttribute.getVariable().toString())));

            elementNode.appendChild(variableName);
            elementNode.appendChild(className);
            elementNode.appendChild(variableValue);

            nodeElement.appendChild(elementNode);
        }

        return nodeElement;
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

    public String getProgramUuid() {
        return this.program.getUuidString();
    }

    public void setProgram(Program program) {
        this.program = program;
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
        if (nextNodeToRun == null) {
            this.nextNodeToRun = "";
        } else {
            this.nextNodeToRun = nextNodeToRun;
        }
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

    /**
     * Generic method to copy a node.
     *
     * @param <Node> Generic node which extends {@link application.node.design.DrawableNode}.
     * @return Returns a new object with same values as this object.
     */
    public <Node extends DrawableNode> Node copy() {
        Class nodeClass = this.getClass();

        Node node = null;

        try {
            Constructor constructor = nodeClass.getDeclaredConstructor(nodeClass);
            node = (Node) constructor.newInstance(this);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }

        return node;
    }

    public Program getProgram() {
        return program;
    }
}
