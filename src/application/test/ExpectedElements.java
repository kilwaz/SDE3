package application.test;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ExpectedElements {
    private List<ExpectedElement> expectedElements = new ArrayList<>();

    private static Logger log = Logger.getLogger(ExpectedElements.class);

    public ExpectedElements() {

    }

    public void add(ExpectedElement element) {
        expectedElements.add(element);
    }

    public Integer size() {
        return expectedElements.size();
    }

    public List<ExpectedElement> getElements() {
        return expectedElements;
    }

    public Boolean containsMatch(ChangedElement changedElement) {
        return false;
    }
}
