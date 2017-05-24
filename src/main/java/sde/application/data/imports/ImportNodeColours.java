package sde.application.data.imports;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import sde.application.data.DataBank;
import sde.application.data.NodeColour;
import sde.application.gui.Controller;
import sde.application.utils.SDERunnable;
import sde.application.utils.SDEUtils;
import sde.application.utils.managers.DataSourceManager;

public class ImportNodeColours extends SDERunnable {
    private static Logger log = Logger.getLogger(ImportNodes.class);
    private Document document;
    private ImportTask importTask = null;

    public ImportNodeColours(Document document, ImportTask importTask) {
        this.document = document;
        this.importTask = importTask;
    }

    private static String getTextValue(String def, Element element, String tag) {
        String value = def;
        NodeList nl;
        nl = element.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return SDEUtils.unescapeXMLCData(value);
    }

    public void threadRun() {
        importTask.started();

        Element element = document.getDocumentElement();

        if (element.getTagName().contains("NodeColours")) {
            NodeList nodeColoursChildNodes = element.getChildNodes();
            for (int i = 0; i < nodeColoursChildNodes.getLength(); i++) {
                if (nodeColoursChildNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element nodeColourElement = (Element) nodeColoursChildNodes.item(i);

                    if (nodeColourElement.getTagName().equals("NodeColour")) {
                        NodeColour nodeColour = NodeColour.create(NodeColour.class);

                        String blue = getTextValue("", nodeColourElement, "Blue");
                        String red = getTextValue("", nodeColourElement, "Red");
                        String green = getTextValue("", nodeColourElement, "Green");
                        String nodeType = getTextValue("", nodeColourElement, "NodeType");

                        nodeColour.setBlue(Integer.parseInt(blue));
                        nodeColour.setRed(Integer.parseInt(red));
                        nodeColour.setGreen(Integer.parseInt(green));
                        nodeColour.setNodeType(nodeType);

                        nodeColour.save();
                    }
                }
            }
        }

        DataSourceManager.getInstance().finaliseTransactions();
        DataBank.getNodeColours().reloadNodeColours();
        Controller.getInstance().updateCanvasControllerLater();

        importTask.setIsFinished(true);
    }
}
