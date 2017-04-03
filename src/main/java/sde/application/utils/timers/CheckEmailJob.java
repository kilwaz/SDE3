package sde.application.utils.timers;

import sde.application.utils.Email;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CheckEmailJob implements Job {
    private static Logger log = Logger.getLogger(CheckEmailJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Email email = (Email) dataMap.get("email");
        email.touchEmail();
    }
}
