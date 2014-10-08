package application.gui;

import javafx.embed.swing.SwingNode;

import javax.swing.*;

public class SwingNodeConsoleTextArea extends SwingNode {
    private SwingNodeConsoleTextArea swingNode = this;

    public SwingNodeConsoleTextArea() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingConsoleTextArea swingConsoleTextArea = new SwingConsoleTextArea();

                swingNode.setContent(swingConsoleTextArea);
                swingNode.resize(500, 200);
            }
        });
    }
}
