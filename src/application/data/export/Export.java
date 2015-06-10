package application.data.export;

public class Export {
    private ExportCell[][] exportValues; // First number is row, second is col, so xy

    private Integer colCount;
    private Integer rowCount;

    public Export(Integer rowCount, Integer colCount) {
        this.colCount = colCount;
        this.rowCount = rowCount;

        exportValues = new ExportCell[rowCount][colCount];
    }

    public void add(ExportCell exportValue) {
        exportValues[exportValue.getRowPosition()][exportValue.getColumnPosition()] = exportValue;
    }

    public ExportCell getValue(Integer rowCount, Integer colCount) {
        return exportValues[rowCount][colCount];
    }

    public ExportCell[] getRow(Integer rowPos) {
        return exportValues[rowPos];
    }

    public Integer getColCount() {
        return colCount;
    }

    public Integer getRowCount() {
        return rowCount;
    }
}
