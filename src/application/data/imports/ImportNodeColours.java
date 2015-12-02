package application.data.imports;

import application.data.DataBank;
import application.data.NodeColour;
import application.gui.Controller;
import application.gui.window.ImportWindow;
import application.utils.SDERunnable;
import application.utils.SDEUtils;
import application.utils.managers.DatabaseTransactionManager;
import application.utils.managers.SessionManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.mail.Session;

public class ImportNodeColours extends SDERunnable {
    private static Logger log = Logger.getLogger(ImportNodes.class);
    private Document document;
    private ImportWindow importWindow;

    public ImportNodeColours(Document document, ImportWindow importWindow) {
        this.document = document;
        this.importWindow = importWindow;
    }

    public void threadRun() {
        Element element = document.getDocumentElement();
        if (importWindow != null) {
            importWindow.startImportProgress();
        }

        if (element.getTagName().contains("NodeColours")) {
            NodeList nodeColoursChildNodes = element.getChildNodes();
            Double importedCount = 1.0;
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

                    if (importWindow != null) {
                        importWindow.updateImportProgress(importedCount / nodeColoursChildNodes.getLength());
                        importedCount++;
                    }
                }
            }
        }

        DatabaseTransactionManager.getInstance().finaliseTransactions();
        DataBank.getNodeColours().reloadNodeColours();
        Controller.getInstance().updateCanvasControllerLater();

        // Closes the import window and update the display to show the new node, also check all connections
        if (importWindow != null) {
            importWindow.endImportProgress();
            importWindow.closeWindow();
        }
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
}
