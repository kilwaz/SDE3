package application.test.action;

import application.data.DataBank;
import application.node.design.DrawableNode;
import application.node.implementations.RequestTrackerNode;
import application.test.TestParameter;

public class TrackAction extends ActionControl {
    // This class is used to connect a RequestTrackerNode to the current WebProxy being used to recode the tests
    public TrackAction() {
    }

    public void performAction() {
        // Add the result to a result node if it is linked
        TestParameter trackerNode = getTestCommand().getParameterByPath("trackerNode");
        if (trackerNode != null) {
            DrawableNode resultNode = DataBank.currentlyEditProgram.getFlowController().getNodeThisControllerFromContainedText(trackerNode.getParameterValue());
            if (resultNode != null && resultNode instanceof RequestTrackerNode) {
                RequestTrackerNode requestTrackerNode = (RequestTrackerNode) resultNode;
                getHttpProxyServer().addRequestTrackerNode(requestTrackerNode);
            }
        }
    }
}





