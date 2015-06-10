package application.data.export;

public class ExportFormula extends ExportCell {
    private String formula;

    public ExportFormula(String formula, Integer rowPosition, Integer columnPosition) {
        super(rowPosition, columnPosition);
        this.formula = formula;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }
}
