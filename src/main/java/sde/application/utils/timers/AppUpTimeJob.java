package sde.application.utils.timers;

import sde.application.utils.managers.StatisticsManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AppUpTimeJob implements Job {
    private static Logger log = Logger.getLogger(AppUpTimeJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        StatisticsManager.getInstance().getTotalStatisticStore().incrementUpTime();
        StatisticsManager.getInstance().getSessionStatisticStore().incrementUpTime();
    }
}
