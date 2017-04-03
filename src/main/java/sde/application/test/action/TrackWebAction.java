package sde.application.test.action;

import sde.application.node.design.DrawableNode;
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
        TestParameter trackerReference = getTestCommand().getParameterByPath("trackerReference");
        if (trackerNode.exists()) {
            DrawableNode resultNode = getProgram().getFlowController().getNodeThisControllerFromContainedText(trackerNode.getParameterValue());
            if (resultNode != null && resultNode instanceof RequestTrackerNode) {
                RequestTrackerNode requestTrackerNode = (RequestTrackerNode) resultNode;
                getHttpProxyServer().addRequestListener(requestTrackerNode);
            }
        }
        if (trackerReference.exists()) {
            getHttpProxyServer().getWebProxyRequestManager().getRecordedProxy().setProxyReference(trackerReference.getParameterValue());
        }
    }
}





