package sde.application.utils.timers;

import sde.application.gui.AceTextArea;
import sde.application.node.implementations.LogicNode;
import sde.application.utils.CompileCode;
import sde.application.utils.CompileResult;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class CompileLogicCodeJob implements Job {
    private static Logger log = Logger.getLogger(TransactionJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        AceTextArea aceTextArea = (AceTextArea) dataMap.get("aceTextArea");
        aceTextArea.setWaitingToTriggerCompile(false);
        if (aceTextArea.getNode() instanceof LogicNode) {
            LogicNode logicNode = (LogicNode) aceTextArea.getNode();
            CompileResult compileResult = CompileCode.compileCode(logicNode.getLogic());
            logicNode.setAceTextAreaCompileErrors(compileResult.getCompiledLineErrors());
        }
    }
}
