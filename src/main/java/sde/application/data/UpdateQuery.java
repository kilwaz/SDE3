package sde.application.data;

import org.apache.log4j.Logger;
import sde.application.utils.managers.DataSourceManager;

import java.util.ArrayList;
import java.util.List;

public class UpdateQuery implements Query {
    private DataSource dataSource;
    private String query;
    private List<Object> parameters = new ArrayList<>();

    private static Logger log = Logger.getLogger(UpdateQuery.class);

    public UpdateQuery(String query) {
        this.dataSource = DataSourceManager.getInstance().findDataSourceByType(DBConnection.CONNECTION_APP);
        this.query = query;
    }

    public Query setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
        return this;
    }

    public Query setTargetConnection(int dataSourceType) {
        this.dataSource = DataSourceManager.getInstance().findDataSourceByType(dataSourceType);
        return this;
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
        dataSource.addUpdate(this);
        //DatabaseTransactionManager.getInstance().addUpdate(this);
        return DataBank.runUpdateQuery(this);
    }

    public DataSource getDataSource() {
        return dataSource;
    }
}
