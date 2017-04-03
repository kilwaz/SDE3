package sde.application.utils.timers;


import sde.application.utils.managers.DatabaseTransactionManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class DeleteDataJob implements Job {
    private static Logger log = Logger.getLogger(TransactionJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        DatabaseTransactionManager.getInstance().scheduledDelete();
    }
}
