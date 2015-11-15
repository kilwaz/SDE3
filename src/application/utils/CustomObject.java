package application.utils;

import application.data.model.DatabaseObject;
import application.node.implementations.CustomObjectNode;

import java.io.InputStream;
import java.util.UUID;

public class CustomObject extends DatabaseObject {
    private CustomObjectNode parentNode = null;
    private Object payload = "";
    private String payLoadReference = "";

    public CustomObject(UUID uuid) {
        super(uuid);
    }

    public CustomObject(UUID uuid, Object payload, String payLoadReference, CustomObjectNode parentNode) {
        super(uuid);
        this.parentNode = parentNode;
        this.payload = payload;
        this.payLoadReference = payLoadReference;
    }

    public Object getPayload() {
        return payload;
    }

    public String getPayLoadReference() {
        return payLoadReference;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
        save();
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
        save();
    }

    public CustomObjectNode getParentNode() {
        return parentNode;
    }
}
