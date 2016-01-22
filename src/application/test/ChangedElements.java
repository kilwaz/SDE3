package application.test;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ChangedElements {
    private List<ChangedElement> changedElements = new ArrayList<ChangedElement>();

    private static Logger log = Logger.getLogger(ChangedElements.class);

    public ChangedElements() {

    }

    public void addElement(ChangedElement element) {
        changedElements.add(element);
    }

    public void printChanges() {
        log.info("Print Changes!");
        for (ChangedElement changedElement : changedElements) {
            changedElement.printChange();
        }
    }

    public Integer size() {
        return changedElements.size();
    }

    public List<ChangedElement> getChangedElements() {
        return changedElements;
    }
}
