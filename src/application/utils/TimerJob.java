package application.utils;


import application.gui.FlowController;
import application.gui.Program;
import application.node.design.DrawableNode;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TimerJob implements Job {
    private DrawableNode nodeToRun;
    private static Logger log = Logger.getLogger(TimerJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        nodeToRun = (DrawableNode) dataMap.get("node");

        if (nodeToRun != null) {
            Program.runHelper(nodeToRun.getContainedText(), FlowController.getFlowControllerFromNode(nodeToRun).getReferenceID(), nodeToRun, false, false, null);
        } else {
            log.info("Node was null when trying to run a task");
        }
    }
}
