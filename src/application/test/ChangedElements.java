package application.test;

import java.util.ArrayList;
import java.util.List;

public class ChangedElements {
    private List<ChangedElement> changedElements = new ArrayList<ChangedElement>();

    public ChangedElements() {

    }

    public void addElement(ChangedElement element) {
        changedElements.add(element);
    }

    public void printChanges() {
        System.out.println("Print Changes!");
        for (ChangedElement changedElement : changedElements) {
            changedElement.printChange();
        }
    }
}
