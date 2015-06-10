package application.node.implementations;

import application.data.export.Export;
import application.data.export.ExportCell;
import application.data.export.ExportFormula;
import application.data.export.ExportValue;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExportNode extends DrawableNode {
    // This will make a copy of the node passed to it
    public ExportNode(ExportNode exportNode) {
        this.setId(-1);
        this.setX(exportNode.getX());
        this.setY(exportNode.getY());
        this.setWidth(exportNode.getWidth());
        this.setHeight(exportNode.getHeight());
        this.setColor(exportNode.getColor());
        this.setScale(exportNode.getScale());
        this.setContainedText(exportNode.getContainedText());
        this.setProgramId(exportNode.getProgramId());
        this.setNextNodeToRun(exportNode.getNextNodeToRun());
    }

    public ExportNode(Integer id, Integer programId) {
        super(id, programId);
    }

    public ExportNode(Double x, Double y, String containedText) {
        super(x, y, 50.0, 40.0, Color.BLACK, containedText, -1, -1);
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        if (nodeRunParams.getOneTimeVariable() instanceof Export) {
            Export export = (Export) nodeRunParams.getOneTimeVariable();

            XSSFWorkbook workbook = new XSSFWorkbook();
            XSSFSheet sheet = workbook.createSheet("Export");

//            SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
//            ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingRule(ComparisonOperator.GT, "0");
//            FontFormatting fontFmt = rule1.createFontFormatting();
//            fontFmt.setFontStyle(true, false);
//            fontFmt.setFontColorIndex(IndexedColors.DARK_RED.index);
//
//            ConditionalFormattingRule[] cfRules = {rule1};
//
//            CellRangeAddress[] regions = {
//                    CellRangeAddress.valueOf("A2:BS59")
//            };
//
//            sheetCF.addConditionalFormatting(regions, cfRules);

            for (Integer row = 0; row < export.getRowCount(); row++) {
                XSSFRow currentRow = sheet.createRow(row);

                for (Integer col = 0; col < export.getColCount(); col++) {
                    ExportCell exportCell = export.getValue(row, col);

                    XSSFCell currentCell = currentRow.createCell(col);

                    if (exportCell instanceof ExportValue) {
                        ExportValue exportValue = (ExportValue) exportCell;

                        // Find the type of value that this is and set it correctly
                        if (exportValue.getDataValue() instanceof String) { // String
                            currentCell.setCellValue((String) exportValue.getDataValue());
                        } else if (exportValue.getDataValue() instanceof Long) { // Long
                            currentCell.setCellValue((Long) exportValue.getDataValue());
                        } else if (exportValue.getDataValue() instanceof Double) { // Double
                            DecimalFormat df2 = new DecimalFormat("###.##");
                            currentCell.setCellValue(Double.valueOf(df2.format(exportValue.getDataValue())));
                        } else if (exportValue.getDataValue() instanceof Integer) { // Integer
                            currentCell.setCellValue((Integer) exportValue.getDataValue());
                        }
                    } else if (exportCell instanceof ExportFormula) {
                        ExportFormula exportFormula = (ExportFormula) exportCell;

                        currentCell.setCellFormula(exportFormula.getFormula());
                        currentCell.setCellErrorValue(FormulaError.NA);

                    }
                }
            }

            for (Integer col = 0; col < export.getColCount(); col++) {
                sheet.autoSizeColumn(col);
            }

            // Setup and create the file location we are going to use
            File exportOutputFile = null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            Date date = new Date();
            String fileDate = dateFormat.format(date);
            try {
                exportOutputFile = new File("C:/Users/alex/Downloads", "/export-" + fileDate + ".xlsx");
                if (!exportOutputFile.exists()) {
                    Boolean createFileResult = exportOutputFile.createNewFile();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            FileOutputStream fos = null;
            try {
                // Try writing the file
                fos = new FileOutputStream(exportOutputFile);
                workbook.write(fos);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        return tab;
    }
}
