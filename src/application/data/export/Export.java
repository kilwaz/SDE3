package application.data.export;

public class Export {
    private ExportValue[][] exportValues; // First number is row, second is col, so xy

    private Integer colCount;
    private Integer rowCount;

    public Export(Integer rowCount, Integer colCount) {
        this.colCount = colCount;
        this.rowCount = rowCount;

        exportValues = new ExportValue[rowCount][colCount];
    }

    public void add(ExportValue exportValue) {
        exportValues[exportValue.getRowPosition()][exportValue.getColumnPosition()] = exportValue;
    }

    public ExportValue getValue(Integer rowCount, Integer colCount) {
        return exportValues[rowCount][colCount];
    }

    public ExportValue[] getRow(Integer rowPos) {
        return exportValues[rowPos];
    }

    public Integer getColCount() {
        return colCount;
    }

    public Integer getRowCount() {
        return rowCount;
    }
}
