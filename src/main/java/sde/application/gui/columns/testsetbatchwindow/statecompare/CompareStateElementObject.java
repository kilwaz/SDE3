package sde.application.gui.columns.testsetbatchwindow.statecompare;

import sde.application.test.ChangedElement;
import sde.application.test.ExpectedElement;

public class CompareStateElementObject {
    private ChangedElement changedElement;
    private ExpectedElement expectedElement;

    private Boolean unexpectedChange = false;

    public CompareStateElementObject(ExpectedElement expectedElement, ChangedElement changedElement) {
        this.changedElement = changedElement;
        this.expectedElement = expectedElement;
    }

    public String getAttributeName() {
        if (changedElement != null) {
            return changedElement.getAttributeName();
        } else if (expectedElement != null) {
            return expectedElement.getAttribute();
        } else {
            return null;
        }
    }

    public String getInitialRef() {
        if (changedElement != null) {
            return changedElement.getInitialRef();
        } else if (expectedElement != null) {
            return expectedElement.getElementReference();
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
            return false;
        }
    }

    public String getChangeType() {
        if (expectedElement != null) {
            return expectedElement.getChangeType();
        } else if (changedElement != null) {
            return changedElement.getChangeType();
        } else {
            return null;
        }
    }

    public void setUnexpectedChange(Boolean unexpectedChange) {
        this.unexpectedChange = unexpectedChange;
    }

    public Boolean isUnexpectedChange() {
        return unexpectedChange;
    }
}
