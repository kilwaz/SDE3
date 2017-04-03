package sde.application.node.objects;

import sde.application.node.implementations.BatchNode;

public class BatchTest {
    private BatchNode parentBatchNode;
    private String text = "";

    public BatchTest(BatchNode batchNode) {
        this.parentBatchNode = batchNode;
        this.text = "";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            if (!parentBatchNode.isInitialising()) {
                parentBatchNode.save();
            }
        }
    }
}
