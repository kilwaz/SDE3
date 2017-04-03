package sde.application.data.export;


import sde.application.error.Error;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.IndexedColors;
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
import java.util.List;

public class ExportBuilder {
    private Export export;
    private File saveLocation;

    public ExportBuilder() {
    }

    public ExportBuilder saveLocation(File saveLocation) {
        this.saveLocation = saveLocation;
        return this;
    }

    public ExportBuilder export(Export export) {
        this.export = export;
        return this;
    }

    public ExportBuilder construct() {
        if (export == null) {
            Error.EXPORT_NOTHING_TO_EXPORT.record().create();
            return this;
        }
        if (saveLocation == null) {
            Error.EXPORT_NO_FILE_SPECIFIED.record().create();
            return this;
        }

        // Write and output the file
        FileOutputStream fos = null;
        try {
            XSSFWorkbook workbook = new XSSFWorkbook();

            List<ExportSheet> exportSheets = export.getExportSheets();

            for (ExportSheet exportSheet : exportSheets) {
                XSSFSheet sheet;
                if(!"sheet".equals(exportSheet.getSheetName())){
                    sheet = workbook.createSheet(exportSheet.getSheetName());
                } else {
                    sheet = workbook.createSheet();
                }

                for (Integer row = 0; row < exportSheet.getRowCount(); row++) {
                    XSSFRow currentRow = sheet.createRow(row);

                    for (Integer col = 0; col < exportSheet.getColCount(); col++) {
                        ExportCell exportCell = exportSheet.getValue(row + 1, col + 1);

                        XSSFCell currentCell = currentRow.createCell(col);

                        // Set the cell colour if it has any assigned
                        if (exportCell != null && exportCell.getCellColour() != null) {
                            CellStyle cellStyle = workbook.createCellStyle();
                            IndexedColors indexedColors = IndexedColors.valueOf(exportCell.getCellColour());
                            cellStyle.setFillForegroundColor(indexedColors.getIndex());
                            cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                            currentCell.setCellStyle(cellStyle);
                        }

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
                            } else if (exportValue.getDataValue() == null) { // If the value passed in is null then we don't do anything
                                currentCell.setCellValue("");
                            } else { // If something else we haven't found call toString
                                currentCell.setCellValue(exportValue.getDataValue().toString());
                            }
                        } else if (exportCell instanceof ExportFormula) {
                            ExportFormula exportFormula = (ExportFormula) exportCell;

                            currentCell.setCellFormula(exportFormula.getFormula());
                            currentCell.setCellErrorValue(FormulaError.NA);
                        }
                    }
                }

                for (Integer col = 0; col < exportSheet.getColCount(); col++) {
                    sheet.autoSizeColumn(col);
                }
            }

            // Setup and create the file location we are going to use
            File exportOutputFile = null;
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            Date date = new Date();
            dateFormat.format(date);

            exportOutputFile = saveLocation;
            if (!exportOutputFile.exists()) {
                Boolean createDirectoryResult = exportOutputFile.getParentFile().mkdirs();
                Boolean createFileResult = exportOutputFile.createNewFile();
            }

            // Try writing the file
            fos = new FileOutputStream(exportOutputFile);
            workbook.write(fos);
        } catch (IOException | NullPointerException ex) {
            Error.RUN_EXPORT_NODE.record().additionalInformation("File:" + saveLocation).create(ex);
        } catch (Exception ex) {
            Error.RUN_EXPORT_NODE.record().additionalInformation("Unexpected Error").create(ex);
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
                Error.CLOSE_FILE.record().create(ex);
            }
        }

        return this;
    }
}
