package application.node.objects;

import application.data.DataBank;
import application.node.implementations.BatchNode;
import application.node.implementations.TestNode;

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
                DataBank.saveNode(parentBatchNode);
            }
        }
    }
}
