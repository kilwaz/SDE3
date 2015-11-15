package application.net.proxy;

import application.data.model.DatabaseObject;

import java.util.UUID;

public class RecordedHeader extends DatabaseObject {
    private RecordedRequest parentRequest;
    private String type = "";
    private String name = "";
    private String value = "";

    public RecordedHeader(UUID uuid, RecordedRequest parentRequest) {
        super(uuid);
        this.parentRequest = parentRequest;
    }

    public String getParentRequestUuid() {
        if (parentRequest != null) {
            return parentRequest.getUuidString();
        }
        return null;
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
}
