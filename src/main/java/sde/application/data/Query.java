package sde.application.data;

import java.util.List;

public interface Query {
    public Query addParameter(Object value);

    public String getQuery();

    public List<Object> getParameters();

    public Object execute();
}
