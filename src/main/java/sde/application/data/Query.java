package sde.application.data;

import java.util.List;

public interface Query {
    Query addParameter(Object value);

    String getQuery();

    List<Object> getParameters();

    Object execute();

    Query setDataSource(DataSource dataSource);

    Query setTargetConnection(int dataSourceType);
}
