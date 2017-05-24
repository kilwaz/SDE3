package sde.application.utils.managers;

import org.apache.log4j.Logger;
import sde.application.data.DataSource;

import java.util.ArrayList;
import java.util.List;

public class DataSourceManager {
    private static DataSourceManager instance;
    private static Logger log = Logger.getLogger(DataSourceManager.class);

    private List<DataSource> dataSources = new ArrayList<>();

    public DataSourceManager() {
        instance = this;
    }

    public static DataSourceManager getInstance() {
        if (instance == null) {
            new DataSourceManager();
        }
        return instance;
    }

    public void removeDataSource(DataSource dataSource) {
        dataSources.remove(dataSource);
    }

    public void addDataSource(DataSource dataSource) {
        dataSources.add(dataSource);
    }

    public DataSource findDataSourceByType(int dataSourceType) {
        // TODO: This should be changed so it doesn't require a search each time we run a query
        List<DataSource> filteredSources = new ArrayList<>();
        for (DataSource dataSource : dataSources) {
            if (dataSourceType == dataSource.getDataSourceType()) {
                filteredSources.add(dataSource);
            }
        }

        if (filteredSources.size() > 1) {
            return filteredSources.get(0);
        } else if (filteredSources.size() == 1) {
            return filteredSources.get(0);
        }

        return null;
    }

    public void finaliseTransactions() {
        for (DataSource dataSource : dataSources) {
            dataSource.finaliseTransactions();
        }
    }

    public void closeAllDataSources() {
        finaliseTransactions();
        for (DataSource dataSource : dataSources) {
            dataSource.getDbConnection().close();
        }
    }
}
