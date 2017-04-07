package sde.application.net.proxy;

import sde.application.data.model.DatabaseObject;

public class RecordedHeader extends DatabaseObject {
    private RecordedRequest parentRequest;
    private String type = "";
    private String name = "";
    private String value = "";

    public RecordedHeader() {
        super();
    }

    public String getParentRequestUuid() {
        if (parentRequest != null) {
            return parentRequest.getUuidString();
        }
        return null;
    }

    public String getParentUuid() {
        if (parentRequest != null) {
            return parentRequest.getUuidString();
        }
        return "";
    }

    public RecordedRequest getParentRequest() {
        return parentRequest;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        if (value == null) {
            return "";
        }
        return value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setParent(RecordedRequest parentRequest) {
        this.parentRequest = parentRequest;
    }
}
