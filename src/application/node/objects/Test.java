package application.node.objects;

import application.data.DataBank;
import application.node.implementations.TestNode;

public class Test {
    private TestNode parentTestNode;
    private String text = "";

    public Test(TestNode testNode) {
        this.parentTestNode = testNode;
        this.text = "";
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            if (!parentTestNode.isInitialising()) {
                DataBank.saveNode(parentTestNode);
            }
        }
    }
}
