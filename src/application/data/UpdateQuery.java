package application.data;

import java.util.ArrayList;
import java.util.List;

public class UpdateQuery implements Query {
    private String query;
    private List<Object> parameters = new ArrayList<>();

    public UpdateQuery(String query) {
        this.query = query;
    }

    public Query addParameter(Object value) {
        parameters.add(value);
        return this;
    }

    public String getQuery() {
        return query;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public Object execute() {
        return DataBank.runUpdateQuery(this);
    }
}