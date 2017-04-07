package sde.application.data;

import java.util.ArrayList;
import java.util.List;

public class SelectResult {
    List<SelectResultRow> results = new ArrayList<>();

    public SelectResult() {
    }

    public void addResultRow(SelectResultRow selectResultRow) {
        results.add(selectResultRow);
    }

    public List<SelectResultRow> getResults() {
        return results;
    }
}
