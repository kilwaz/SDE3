package application.gui.columns.testsetbatchwindow.statecompare;

import application.test.ChangedElement;
import application.test.ExpectedElement;

public class CompareStateElementObject {
    private ChangedElement changedElement;
    private ExpectedElement expectedElement;

    public CompareStateElementObject(ChangedElement changedElement) {
        this.changedElement = changedElement;
    }

    public CompareStateElementObject(ExpectedElement expectedElement) {
        this.expectedElement = expectedElement;
    }

    public String getAttributeName() {
        if (changedElement != null) {
            return changedElement.getAttributeName();
        } else {
            return null;
        }
    }

    public String getInitialRef() {
        if (changedElement != null) {
            return changedElement.getInitialRef();
        } else {
            return null;
        }
    }

    public String getInitialValue() {
        if (changedElement != null) {
            return changedElement.getInitialValue();
        } else {
            return null;
        }
    }

    public String getFinalValue() {
        if (changedElement != null) {
            return changedElement.getFinalValue();
        } else {
            return null;
        }
    }

    public String getBefore() {
        if (expectedElement != null) {
            return expectedElement.getBefore();
        } else {
            return null;
        }
    }

    public String getAfter() {
        if (expectedElement != null) {
            return expectedElement.getAfter();
        } else {
            return null;
        }
    }

    public Double getIncreasedBy() {
        if (expectedElement != null) {
            return expectedElement.getIncreasedBy();
        } else {
            return null;
        }
    }

    public Boolean getMatched() {
        if (expectedElement != null) {
            return expectedElement.getMatched();
        } else {
            return null;
        }
    }

    public String getChangeType() {
        if (expectedElement != null) {
            return expectedElement.getChangeType();
        } else {
            return null;
        }
    }
}
