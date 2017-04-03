package sde.application.net.proxy;

public class RecordedParameter {
    private RecordedRequest parentRequest;
    private String name = "";
    private String value = "";

    public RecordedParameter() {
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        if (value == null) {
            return "";
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setParent(RecordedRequest parentRequest) {
        this.parentRequest = parentRequest;
    }
}

