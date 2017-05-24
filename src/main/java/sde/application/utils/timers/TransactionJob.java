package sde.application.utils.timers;


import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import sde.application.data.DataSource;

public class TransactionJob implements Job {
    private static Logger log = Logger.getLogger(TransactionJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        DataSource dataSource = (DataSource) dataMap.get("dataSource");
        dataSource.checkIfNeedToFinalise();
    }
}
