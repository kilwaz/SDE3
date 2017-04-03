package sde.application.test.action.helpers;

import java.util.HashMap;

public class StateTracker {

    private HashMap<String, PageStateCapture> pageStates = new HashMap<>();

    public PageStateCapture getState(String ref) {
        return pageStates.get(ref);
    }

    public void removeState(String ref) {
        pageStates.remove(ref);
    }

    public void setState(PageStateCapture pageState) {
        pageStates.put(pageState.getStateName(), pageState);
    }
}
