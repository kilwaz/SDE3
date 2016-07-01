package application.test;

import application.error.Error;
import application.gui.columns.testsetbatchwindow.statecompare.CompareStateElementObject;
import application.test.action.helpers.PageStateCapture;
import application.test.annotation.AssertChange;
import application.test.core.AssertData;
import application.test.core.TestCase;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.log4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Collectors;

public class PageStateCompare {
    private static Logger log = Logger.getLogger(PageStateCompare.class);
    private ExpectedElements expectedElements = new ExpectedElements();
    private ChangedElements changedElements = new ChangedElements();
    private PageStateCapture captureBefore = null;
    private PageStateCapture captureAfter = null;
    private TestCase parentTestCase;
    private StringProperty reference = new SimpleStringProperty();
    private ObservableList<CompareStateElementObject> compareStateElementObjects = null;

    public PageStateCompare(TestCase parentTestCase, PageStateCapture captureBefore, PageStateCapture captureAfter) {
        this.captureBefore = captureBefore;
        this.captureAfter = captureAfter;
        this.parentTestCase = parentTestCase;
        reference.set("Test Reference");
    }

    public String getReference() {
        return reference.get();
    }

    public StringProperty referenceProperty() {
        return reference;
    }

    public ExpectedElements getExpectedElements() {
        return expectedElements;
    }

    public ChangedElements getChangedElements() {
        return changedElements;
    }

    public ObservableList<CompareStateElementObject> getElementsList() {
        if (compareStateElementObjects == null) {
            compareStateElementObjects = FXCollections.observableArrayList();
        }

        return compareStateElementObjects;
    }

    public void compareTest() {
        if (parentTestCase.getTest() != null && captureBefore != null && captureAfter != null) {
            // This needs to be done after the test has completed as some of the assertions require knowledge of elements from the test results
            for (Method method : parentTestCase.getTemplateCaseClass().getDeclaredMethods()) {
                if (method.isAnnotationPresent(AssertChange.class)) {
                    Annotation annotation = method.getAnnotation(AssertChange.class);
                    AssertChange assertChange = (AssertChange) annotation;
                    ExpectedElement expectedElement = ExpectedElement.define();

                    // id
                    if (!assertChange.id().isEmpty()) {
                        expectedElement.id(assertChange.id());
                    }

                    // type
                    if (!assertChange.type().isEmpty()) {
                        expectedElement.type(assertChange.type());
                    }

                    // attribute
                    if (!assertChange.attribute().isEmpty()) {
                        expectedElement.attribute(assertChange.attribute());
                    }

                    // before
                    if (!assertChange.before().isEmpty()) {
                        expectedElement.before(assertChange.before());
                    }

                    // after
                    if (!assertChange.after().isEmpty()) {
                        expectedElement.after(assertChange.after());
                    }

                    // increasedBy
                    if (!assertChange.increasedBy().isEmpty()) {
                        expectedElement.increasedBy(assertChange.increasedBy());
                    }

                    AssertData assertData = new AssertData();
                    assertData.expectedElement(expectedElement).inputs(parentTestCase.getInputs());
                    assertData.before(captureBefore).after(captureAfter);
                    assertData.states(parentTestCase.getPageCaptures());

                    try {
                        method.invoke(parentTestCase.getTemplateObject(), assertData);
                    } catch (IllegalAccessException | InvocationTargetException ex) {
                        Error.TEST_ANNOTATION_MISSING.record().create(ex);
                    }

                    expectedElements.add(expectedElement);
                }
            }

            // Finally compare the results
            changedElements = captureBefore.compare(captureAfter);

            // Matching up the changed and expected elements
            for (ChangedElement changedElement : changedElements.getElements()) {
                ExpectedElement expectedElement = expectedElements.getMatch(changedElement);
            }

            getElementsList().addAll(expectedElements.getElements().stream().map(CompareStateElementObject::new).collect(Collectors.toList()));
            getElementsList().addAll(changedElements.getElements().stream().map(CompareStateElementObject::new).collect(Collectors.toList()));
        }
    }
}
