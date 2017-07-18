package sde.application.net.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NetworkObject implements Serializable {
    private Integer objectType = -1;

    public NetworkObject() {
        objectType = ObjectTypes.resolveObjectType(this);
    }

    public Integer getObjectType() {
        return objectType;
    }

    protected void setObjectType(Integer objectType) {
        this.objectType = objectType;
    }

    public List<NetworkObject> getResponses() {
        return new ArrayList<>();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }
}
