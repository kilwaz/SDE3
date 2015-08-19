package application.utils.managers;


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
            log.error(ex);
        }

        instance = this;
    }

    public void closeAllJobs() {
        try {
            scheduler.shutdown();
        } catch (SchedulerException ex) {
            log.error(ex);
        }
    }

    public void scheduleJob(JobDetail jobDetail, Trigger trigger) {
        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException ex) {
            log.error(ex);
        }
    }

    public static JobManager getInstance() {
        return instance;
    }
}
