package application.data.export;

import application.Main;
import application.test.ExpectedElement;
import application.test.PageStateCompare;
import application.test.core.TestCase;
import application.test.core.TestSet;
import application.test.core.TestSetBatch;
import javafx.collections.ObservableList;
import javafx.stage.FileChooser;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

public class CreateTestResultExcel {
    private static Logger log = Logger.getLogger(CreateTestResultExcel.class);

    public static void outputExcelTestResults(TestSetBatch testSetBatch) {
        // Choose the destination for the file

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save test results");
        fileChooser.setInitialFileName("Test result " + testSetBatch.getFormattedTime().replace(":", "-") + ".xlsx");

        File saveFile = fileChooser.showSaveDialog(Main.getInstance().getMainStage());

        // Create the exportSheet value
        ExportSheet exportSheet = new ExportSheet();
        exportSheet.setSheetName("Test results");
        exportSheet.add(new ExportValue("Test ID", 1, 1));
        exportSheet.add(new ExportValue("Test Description", 1, 2));
        exportSheet.add(new ExportValue("Test Iteration", 1, 3));
        exportSheet.add(new ExportValue("Passed", 1, 4));
        exportSheet.add(new ExportValue("Failed", 1, 5));
        exportSheet.add(new ExportValue("Total", 1, 6));

        for (TestSet testSet : testSetBatch.getTestSets()) {
            Integer currentRow = exportSheet.getNextFreeRow();
            exportSheet.add(new ExportValue(testSet.getTestID(), currentRow, 1));
            exportSheet.add(new ExportValue(testSet.getTestDescription(), currentRow, 2));
            for (TestCase testCase : (List<TestCase>) testSet.getTestCases()) {
                currentRow = exportSheet.getNextFreeRow();
                exportSheet.add(new ExportValue(testCase.getTestIterationID(), currentRow, 3));

                // Put this code into TestCase?
                Integer totalCompares = 0;
                Integer totalPassed = 0;
                Integer totalFailed = 0;
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

                exportSheet.add(new ExportValue(totalPassed, currentRow, 4));
                exportSheet.add(new ExportValue(totalFailed, currentRow, 5));
                exportSheet.add(new ExportValue(totalCompares, currentRow, 6));
            }
        }

        Export export = new Export();
        export.addSheet(exportSheet);

        new ExportBuilder().export(export).saveLocation(saveFile).construct();
    }
}
