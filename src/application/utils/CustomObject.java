package application.utils;

import application.data.DataBank;
import application.node.implementations.CustomObjectNode;

public class CustomObject {
    private Integer id = -1;
    private CustomObjectNode parentNode = null;
    private Object payload = "";
    private String payLoadReference = "";

    public CustomObject(Integer id, Object payload, String payLoadReference, CustomObjectNode parentNode) {
        this.parentNode = parentNode;
        this.payload = payload;
        this.id = id;
        this.payLoadReference = payLoadReference;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Object getPayload() {
        return payload;
    }

    public String getPayLoadReference() {
        return payLoadReference;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
        DataBank.saveCustomObject(this);
    }

    public void setPayLoadReference(String payLoadReference) {
        this.payLoadReference = payLoadReference;
        DataBank.saveCustomObject(this);
    }

    public CustomObjectNode getParentNode() {
        return parentNode;
    }
}
