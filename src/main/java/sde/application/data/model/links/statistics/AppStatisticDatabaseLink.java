package sde.application.data.model.links.statistics;

import sde.application.data.model.DatabaseLink;
import sde.application.utils.StatisticStore;

import java.util.UUID;

public class AppStatisticDatabaseLink extends DatabaseLink {
    public AppStatisticDatabaseLink() {
        super("app_statistics", StatisticStore.class);

        link("uuid", method("getUuidString"), method("setUuid", UUID.class)); // 1
        link("total_requests", method("getTotalRequests"), method("setTotalRequests", long.class)); // 1
    }
}
