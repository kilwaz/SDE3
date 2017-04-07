package sde.application.data.imports;

import sde.application.data.export.Export;
import sde.application.data.export.ExportSheet;
import sde.application.data.export.ExportValue;
import sde.application.error.Error;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;

public class ImportBuilder {
    private static Logger log = Logger.getLogger(ImportBuilder.class);

    private Export importedExport = null;
    private File loadFile = null;

    public ImportBuilder() {

    }

    public ImportBuilder loadFile(File loadFile) {
        this.loadFile = loadFile;
        return this;
    }

    public Export getExport() {
        return importedExport;
    }

    public ImportBuilder construct() {
        importedExport = new Export();

        if (loadFile != null) {
            try {
                InputStream inp = new FileInputStream(loadFile);
                try {
                    XSSFWorkbook workbook = new XSSFWorkbook(inp);
                    for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
                        XSSFSheet sheet = workbook.getSheetAt(sheetNum);
                        ExportSheet exportSheet = new ExportSheet();

                        for (int rowNum = 0; rowNum < sheet.getPhysicalNumberOfRows(); rowNum++) {
                            XSSFRow row = sheet.getRow(rowNum);
                            for (int cellNum = 0; cellNum < row.getPhysicalNumberOfCells(); cellNum++) {
                                XSSFCell cell = row.getCell(cellNum);
                                if (cell != null) {
                                    switch (cell.getCellType()) {
                                        case 0: // Numeric values
                                            exportSheet.add(new ExportValue(cell.getNumericCellValue(), rowNum + 1, cellNum + 1));
                                            break;
                                        case 1: // String values
                                            exportSheet.add(new ExportValue(cell.getStringCellValue(), rowNum + 1, cellNum + 1));
                                            break;
                                        case 3: // Blank values maybe?
                                            //exportSheet.add(new ExportValue(cell.getNumericCellValue(), rowNum + 1, cellNum + 1));
                                            break;
                                        default:
                                            log.info("Unknown type");
                                    }
                                }
                            }
                        }

                        importedExport.addSheet(exportSheet);
                    }
                } catch (IOException ex) {
                    Error.IMPORT_FILE_FORMAT_EXCEPTION.record().create(ex);
                }
            } catch (FileNotFoundException ex) {
                Error.IMPORT_FILE_NOT_FOUND.record().create(ex);
            }
        } else {
            Error.IMPORT_FILE_NOT_SPECIFIED.record().create();
        }

        return this;
    }
}
