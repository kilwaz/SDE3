package sde.application.data.model;

import java.util.ArrayList;
import java.util.List;

public class ModelChildChain {
    private DatabaseObject baseObject;
    private List<ModelChild> chain = new ArrayList<>();

    public ModelChildChain(DatabaseObject baseObject) {
        this.baseObject = baseObject;
    }

    public void addChild(ModelChild modelChild) {
        chain.add(modelChild);
    }

    public List<ModelChild> getChain() {
        return chain;
    }

    public DatabaseObject getBaseObject() {
        return baseObject;
    }

    public ModelChild getLastInChain() {
        if (chain.isEmpty()) {
            return null;
        }
        return chain.get(chain.size() - 1);
    }

    public void setChain(List<ModelChild> chain) {
        this.chain = chain;
    }

    public ModelChildChain duplicate() {
        ModelChildChain modelChildChain = new ModelChildChain(baseObject);
        modelChildChain.setChain(new ArrayList<>(chain));
        return modelChildChain;
    }

    public DatabaseLink getBaseObjectDatabaseLink() {
        return DatabaseLink.getNewInstanceFromBaseClass(baseObject.getClass());
    }

    public ModelChild getParentChild(ModelChild modelChild) {
        int index = chain.indexOf(modelChild);
        if (index == 0) {
            return null;
        }
        return chain.get(index - 1);
    }

    public Integer getChildPosition(ModelChild modelChild) {
        return chain.indexOf(modelChild);
    }
}
