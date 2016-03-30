package application.utils.timers;


import application.gui.FlowController;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.utils.NodeRunParams;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class TimerNodeJob implements Job {
    private static Logger log = Logger.getLogger(TimerNodeJob.class);
    private DrawableNode nodeToRun;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        nodeToRun = (DrawableNode) dataMap.get("node");

        if (nodeToRun != null) {
            Program.runHelper(nodeToRun.getContainedText(), FlowController.getFlowControllerFromNode(nodeToRun).getReferenceID(), nodeToRun, false, false, new NodeRunParams());
        } else {
            log.info("Node was null when trying to run a task");
        }
    }
}
