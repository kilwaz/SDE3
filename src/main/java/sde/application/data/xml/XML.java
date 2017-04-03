package sde.application.data.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface XML {
    public Document getXMLRepresentation();

    public Element getXMLRepresentation(Document document);
}
