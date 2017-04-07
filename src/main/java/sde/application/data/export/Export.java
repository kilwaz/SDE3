package sde.application.data.export;

import java.util.ArrayList;
import java.util.List;

public class Export {
    private List<ExportSheet> exportSheets = new ArrayList<>();

    public Export() {

    }

    public void addSheet(ExportSheet exportSheet) {
        exportSheets.add(exportSheet);
    }

    public List<ExportSheet> getExportSheets() {
        return exportSheets;
    }
}
