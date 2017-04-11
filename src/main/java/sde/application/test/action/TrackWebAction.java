package sde.application.test.action;

import sde.application.node.design.DrawableNode;
import sde.application.node.implementations.LogTrackerNode;
import sde.application.node.implementations.RequestTrackerNode;
import sde.application.test.TestParameter;

/**
 * This action links up the current test to a {@link RequestTrackerNode}.
 * <p>
 * After being linked all the of requests will be tracked and available to view against the node.
 */
public class TrackWebAction extends WebAction {
    // This class is used to connect a RequestTrackerNode to the current WebProxy being used to recode the tests
    public TrackWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        // Add the result to a result node if it is linked
        TestParameter trackerNode = getTestCommand().getParameterByPath("trackerNode");
        TestParameter logTrackerNode = getTestCommand().getParameterByPath("logTrackerNode");
        TestParameter trackerReference = getTestCommand().getParameterByPath("trackerReference");
        TestParameter logTrackerReference = getTestCommand().getParameterByPath("logTrackerReference");
        if (trackerNode.exists()) {
            DrawableNode listenerNode = getProgram().getFlowController().getNodeThisControllerFromContainedText(trackerNode.getParameterValue());
            if (listenerNode != null && listenerNode instanceof RequestTrackerNode) {
                RequestTrackerNode requestTrackerNode = (RequestTrackerNode) listenerNode;
                getHttpProxyServer().addRequestListener(requestTrackerNode);
            }
        }
        if (trackerReference.exists()) {
            getHttpProxyServer().getWebProxyRequestManager().getRecordedProxy().setProxyReference(trackerReference.getParameterValue());
        }
        if (logTrackerNode.exists()) {
            DrawableNode listenerNode = getProgram().getFlowController().getNodeThisControllerFromContainedText(logTrackerNode.getParameterValue());
            if (listenerNode != null && listenerNode instanceof LogTrackerNode) {
                LogTrackerNode logTrackerNode1 = (LogTrackerNode) listenerNode;
                getTestRunner().addBrowserLogListener(logTrackerNode1);
            }
        }
    }
}





