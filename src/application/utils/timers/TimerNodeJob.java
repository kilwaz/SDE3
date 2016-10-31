package application.utils.timers;


import application.gui.Controller;
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

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();

        DrawableNode nodeToRun = (DrawableNode) dataMap.get("node");

        if (nodeToRun != null) {
            Controller.getInstance().createNotification("Job Started", "Running " + nodeToRun.getContainedText());
            Program.runHelper(nodeToRun.getContainedText(), FlowController.getFlowControllerFromNode(nodeToRun).getReferenceID(), nodeToRun, false, true, null, new NodeRunParams());
        } else {
            log.info("Node was null when trying to run a task");
        }
    }
}
