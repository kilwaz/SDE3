package application.utils;

import application.data.model.DatabaseObject;
import application.node.implementations.CustomObjectNode;

import java.io.InputStream;

public class CustomObject extends DatabaseObject {
    private CustomObjectNode parentNode = null;
    private Object payload = "";
    private String payLoadReference = "";

    public CustomObject() {
        super();
    }

    public Object getPayload() {
        return payload;
    }

    public String getPayLoadReference() {
        return payLoadReference;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    public InputStream getPayLoadInputStream() {
        return Serializer.serializeToInputStream(getPayload());
    }

    public void setPayLoadFromInputStream(InputStream inputStream) {
        payload = Serializer.deserialize(inputStream);
    }

    public String getParentUuid() {
        if (parentNode != null) {
            return parentNode.getUuidString();
        }
        return null;
    }

    public void setPayLoadReference(String payLoadReference) {
        this.payLoadReference = payLoadReference;
    }

    public void setParent(CustomObjectNode parentNode) {
        this.parentNode = parentNode;
    }

    public CustomObjectNode getParentNode() {
        return parentNode;
    }
}
