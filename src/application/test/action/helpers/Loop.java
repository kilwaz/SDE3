package application.test.action.helpers;

import java.util.ArrayList;
import java.util.List;

public class Loop {
    private Integer startLineNumber = 0;
    private Integer loopUntil = 0;
    private Integer currentLoopCount = 0;
    private Boolean continueToLoop = true;
    private String loopType = "";
    private List<LoopedObject> loopElements = new ArrayList<>();

    public Loop(Integer startLineNumber) {
        this.startLineNumber = startLineNumber;
    }

    public Integer getStartLineNumber() {
        return startLineNumber;
    }

    public Boolean getContinueToLoop() {
        return continueToLoop;
    }

    public void setContinueToLoop(Boolean continueToLoop) {
        this.continueToLoop = continueToLoop;
    }

    public void setStartLineNumber(Integer startLineNumber) {
        this.startLineNumber = startLineNumber;
    }

    public Integer getLoopUntil() {
        return loopUntil;
    }

    public void setLoopUntil(Integer loopUntil) {
        this.loopUntil = loopUntil;
    }

    public String getLoopType() {
        return loopType;
    }

    public void setLoopType(String loopType) {
        this.loopType = loopType;
    }

    public Integer getCurrentLoopCount() {
        return currentLoopCount;
    }

    public void setCurrentLoopCount(Integer currentLoopCount) {
        this.currentLoopCount = currentLoopCount;
    }

    public List<LoopedObject> getLoopElements() {
        return loopElements;
    }

    public void setLoopElements(List<LoopedObject> loopElements) {
        this.loopElements = loopElements;
    }

    public LoopedObject getCurrentLoopObject() {
        if (loopElements.size() > 0) {
            return loopElements.get(currentLoopCount - 1);
        } else {
            return null;
        }
    }
}
