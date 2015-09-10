package application.node.implementations;

import application.Main;
import application.data.DataBank;
import application.data.SavableAttribute;
import application.data.export.Export;
import application.data.export.ExportCell;
import application.data.export.ExportFormula;
import application.data.export.ExportValue;
import application.gui.Controller;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.FormulaError;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ExportNode extends DrawableNode {
    private String fileOutputDirectory = "";
    private String fileOutputName = "";
    private String constructedFileName = "";

    private TextField directoryField;
    private Label constructedFileNameLabel;

    private static Logger log = Logger.getLogger(ExportNode.class);

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

        this.setFileOutputDirectory(exportNode.getFileOutputDirectory());
        this.setFileOutputName(exportNode.getFileOutputName());
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
            FileOutputStream fos = null;
            try {
                constructedFileName = fileOutputDirectory + "/" + fileOutputName + ".xlsx";
                constructedFileName = constructedFileName.replace("[DATE]", fileDate);

                exportOutputFile = new File(constructedFileName);
                if (!exportOutputFile.exists()) {
                    Boolean createFileResult = exportOutputFile.createNewFile();
                }

                // Try writing the file
                fos = new FileOutputStream(exportOutputFile);
                workbook.write(fos);
            } catch (IOException ex) {
                log.error(ex);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        }
    }

    public Tab createInterface() {
        Controller controller = Controller.getInstance();

        Tab tab = controller.createDefaultNodeTab(this);
        AnchorPane anchorPane = (AnchorPane) tab.getContent();

        // DIRECTORY ROW
        Label directoryLabel = new Label();
        directoryLabel.setText("Output Directory: ");
        directoryLabel.setMinWidth(130);

        directoryField = new TextField();
        directoryField.setText(fileOutputDirectory);
        directoryField.setPrefWidth(250);
        directoryField.setOnAction(event -> {
            TextField sourceTextField = (TextField) event.getSource();
            fileOutputDirectory = sourceTextField.getText();
            buildConstructedFileName();
            DataBank.saveNode(this);
        });

        Button fileChooserButton = new Button();
        fileChooserButton.setText("Choose Output Directory...");
        fileChooserButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose Output Directory");
            File startDirectory = new File(fileOutputDirectory);
            if (startDirectory.exists()) {
                directoryChooser.setInitialDirectory(startDirectory);
            }
            File selectedDirectory = directoryChooser.showDialog(Main.getInstance().getMainStage());

            if (selectedDirectory != null) {
                fileOutputDirectory = selectedDirectory.getAbsolutePath();
                fileOutputDirectory = fileOutputDirectory.replaceAll("\\\\", "/");
                directoryField.setText(fileOutputDirectory);
                buildConstructedFileName();
                DataBank.saveNode(this);
            }
        });

        HBox directoryRow = new HBox(5);
        directoryRow.setAlignment(Pos.CENTER_LEFT);
        directoryRow.getChildren().add(directoryLabel);
        directoryRow.getChildren().add(directoryField);
        directoryRow.getChildren().add(fileChooserButton);

        // OUTPUT FILE NAME
        Label outputFileNameLabel = new Label();
        outputFileNameLabel.setText("Output File Name: ");
        outputFileNameLabel.setMinWidth(130);

        TextField outputFileNameField = new TextField();
        outputFileNameField.setText(fileOutputName);
        outputFileNameField.setPrefWidth(250);
        outputFileNameField.setOnAction(event -> {
            TextField sourceTextField = (TextField) event.getSource();
            fileOutputName = sourceTextField.getText();
            buildConstructedFileName();
            DataBank.saveNode(this);
        });

        Label dateHelpLabel = new Label();
        dateHelpLabel.setText("Use [DATE] to replace with current date/time");

        HBox outputFileNameRow = new HBox(5);
        outputFileNameRow.setAlignment(Pos.CENTER_LEFT);
        outputFileNameRow.getChildren().add(outputFileNameLabel);
        outputFileNameRow.getChildren().add(outputFileNameField);
        outputFileNameRow.getChildren().add(dateHelpLabel);

        // CONSTRUCTED FILE NAME
        Label constructedFileNameLabel = new Label();
        constructedFileNameLabel.setText("Constructed File Name: ");
        constructedFileNameLabel.setMinWidth(130);

        this.constructedFileNameLabel = new Label();
        buildConstructedFileName();

        HBox constructedFileNameRow = new HBox(5);
        constructedFileNameRow.setAlignment(Pos.CENTER_LEFT);
        constructedFileNameRow.getChildren().add(constructedFileNameLabel);
        constructedFileNameRow.getChildren().add(this.constructedFileNameLabel);

        // ALL ROWS
        VBox rows = new VBox(5);
        rows.setLayoutY(55);
        rows.setLayoutX(11);

        rows.getChildren().add(directoryRow);
        rows.getChildren().add(outputFileNameRow);
        rows.getChildren().add(constructedFileNameRow);

        anchorPane.getChildren().add(rows);

        return tab;
    }

    private void buildConstructedFileName() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        Date date = new Date();
        String fileDate = dateFormat.format(date);

        constructedFileName = fileOutputDirectory + "/" + fileOutputName + ".xlsx";
        constructedFileName = constructedFileName.replace("[DATE]", fileDate);

        constructedFileNameLabel.setText(constructedFileName);
    }

    public List<SavableAttribute> getDataToSave() {
        List<SavableAttribute> savableAttributes = new ArrayList<>();

        savableAttributes.add(new SavableAttribute("FileOutputDirectory", fileOutputDirectory.getClass().getName(), fileOutputDirectory));
        savableAttributes.add(new SavableAttribute("FileOutputName", fileOutputName.getClass().getName(), fileOutputName));
        savableAttributes.addAll(super.getDataToSave());

        return savableAttributes;
    }

    public String getFileOutputDirectory() {
        return fileOutputDirectory;
    }

    public void setFileOutputDirectory(String fileOutputDirectory) {
        this.fileOutputDirectory = fileOutputDirectory;
    }

    public String getFileOutputName() {
        return fileOutputName;
    }

    public void setFileOutputName(String fileOutputName) {
        this.fileOutputName = fileOutputName;
    }

    public String getConstructedFileName() {
        return constructedFileName;
    }
}
