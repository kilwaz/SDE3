package application.gui.columns.testsetbatchwindow.treeview;

import application.test.ExpectedElement;
import application.test.PageStateCompare;
import application.test.core.TestCase;
import application.test.core.TestSet;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import org.apache.log4j.Logger;

public class TestCaseTreeObject {
    public static final Integer UNKNOWN = -1;
    public static final Integer TEST_ROOT = 1;
    public static final Integer TEST_SET = 2;
    public static final Integer TEST_CASE = 3;
    private static Logger log = Logger.getLogger(TestCaseTreeObject.class);
    private TestCase testCase;
    private TestSet testSet;
    private Integer type = UNKNOWN;

    private TestCaseTreeObject() {
        this.type = TEST_ROOT;
    }

    private TestCaseTreeObject(TestSet testSet) {
        this.testSet = testSet;
        this.type = TEST_SET;
    }

    private TestCaseTreeObject(TestCase testCase) {
        this.testCase = testCase;
        this.type = TEST_CASE;
    }

    public static TreeItem<TestCaseTreeObject> createTreeItem() {
        return new TreeItem<>(new TestCaseTreeObject());
    }

    public static TreeItem<TestCaseTreeObject> createTreeItem(TestSet testSet) {
        return new TreeItem<>(new TestCaseTreeObject(testSet));
    }

    public static TreeItem<TestCaseTreeObject> createTreeItem(TestCase testCase) {
        return new TreeItem<>(new TestCaseTreeObject(testCase));
    }

    public Integer getType() {
        return type;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public TestSet getTestSet() {
        return testSet;
    }

    public SimpleIntegerProperty getPassedTestCases() {
        return testCase.passedTestCasesProperty();
    }

    public String getName() {
        if (testSet != null) {  // Root for test set group
            return "Case " + testSet.getTestID();
        } else if (testCase != null) {// Test case provided
            return "Iteration " + testCase.getTestIterationID();
        } else {
            return "Tests";
        }
    }

    public WritableImage getImageRepresentation() {
        Integer imageTotalWidth = 100;
        Integer imageTotalHeight = 20;

        WritableImage imageFX = new WritableImage(imageTotalWidth, imageTotalHeight);
        PixelWriter pw = imageFX.getPixelWriter();

        Integer totalCompares = 0;
        Integer totalPassed = 0;
        Integer totalFailed = 0;
        if (testCase != null) {
            for (PageStateCompare pageStateCompare : (ObservableList<PageStateCompare>) testCase.getPageCapturesCompares()) {
                totalCompares += pageStateCompare.getExpectedElements().getElements().size();
                for (ExpectedElement expectedElement : pageStateCompare.getExpectedElements().getElements()) {
                    if (expectedElement.getMatched()) {
                        totalPassed++;
                    } else {
                        totalFailed++;
                    }
                }
            }
        }

        Double sectionSize = imageTotalWidth.doubleValue() / totalCompares.doubleValue();
        Double passedSize = sectionSize * totalPassed;
        Double failedSize = sectionSize * totalFailed;

        Color green = Color.GREEN;
        Color red = Color.RED;

        if (totalCompares > 0) {
            //log.info("Total " + totalCompares + " passed " + totalPassed + " failed " + totalFailed + " section size = " + sectionSize + " passedSize " + passedSize + " failedSize " + failedSize);

            for (int passedX = 0; passedX < passedSize; passedX++) { // Drawing the passed section
                drawVerticalLine(passedX, imageTotalHeight, pw, green);
            }

            for (int failedX = passedSize.intValue(); failedX < passedSize + failedSize; failedX++) { // Drawing the failed section
                drawVerticalLine(failedX, imageTotalHeight, pw, red);
            }
        }

        return imageFX;
    }

    private void drawVerticalLine(Integer imageX, Integer imageTotalHeight, PixelWriter pw, Color drawColour) {
        //log.info("Drawing row " + imageX + " as " + drawColour);
        for (int i = 0; i < imageTotalHeight; i++) { // Go from top to bottom of image in lines
            pw.setColor(imageX, i, drawColour);
        }
    }
}
