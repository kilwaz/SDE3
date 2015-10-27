package application.utils.managers;


import application.error.*;
import application.error.Error;
import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public class JobManager {
    private static Scheduler scheduler;
    private static JobManager instance;

    private static Logger log = Logger.getLogger(JobManager.class);

    public JobManager() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
        } catch (SchedulerException ex) {
            Error.JOB_MANAGER.record().create(ex);
        }

        instance = this;
    }

    public void closeAllJobs() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            Error.CLOSE_JOBS.record().create(ex);
        }
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException ex) {
            Error.SCHEDULE_JOB.record().create(ex);
        }
    }

    public static JobManager getInstance() {
        return instance;
    }
}
