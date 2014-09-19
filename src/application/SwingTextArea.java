package application;

import application.utils.DataBank;
import javafx.embed.swing.SwingNode;
import org.fife.ui.autocomplete.*;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.util.ArrayList;
import java.util.List;

public class SwingTextArea extends SwingNode implements Runnable {
    private RSyntaxTextArea sourceCodeTextArea;
    private DocumentListener sourceCodeTextAreaListener;
    private Source source = null;

    public SwingTextArea() {
    }

    public void setEnabled(Boolean flag) {
        if (sourceCodeTextArea != null) {
            sourceCodeTextArea.setEnabled(flag);
        }
    }

    public void setSource(Source source) {
        this.source = source;

        if (sourceCodeTextArea != null) {
            sourceCodeTextArea.getDocument().removeDocumentListener(sourceCodeTextAreaListener);
            sourceCodeTextAreaListener = null;

            if (source.getSource() != null) {
                sourceCodeTextArea.setText(source.getSource());
            }

            // Reactivates Listener
            sourceCodeTextAreaListener = new SourceCodeTextAreaDocumentListener();
            sourceCodeTextArea.getDocument().addDocumentListener(sourceCodeTextAreaListener);
        }
    }

    @Override
    public void run() {
        JPanel cp = new JPanel(new java.awt.BorderLayout());

        sourceCodeTextArea = new RSyntaxTextArea(1, 1);
        sourceCodeTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        sourceCodeTextArea.setCodeFoldingEnabled(true);
        sourceCodeTextArea.setAntiAliasingEnabled(true);
        sourceCodeTextArea.setEnabled(true);
        RTextScrollPane sp = new RTextScrollPane(sourceCodeTextArea);
        sp.setFoldIndicatorEnabled(true);
        cp.add(sp);

        if (source != null) {
            sourceCodeTextArea.setText(source.getSource());
        }

        sourceCodeTextAreaListener = new SourceCodeTextAreaDocumentListener();
        sourceCodeTextArea.getDocument().addDocumentListener(sourceCodeTextAreaListener);

//        // Whether templates are enabled is a global property affecting all
//        // RSyntaxTextAreas, so this method is static.
//        RSyntaxTextArea.setTemplatesEnabled(true);
//
//        // Code templates are shared among all RSyntaxTextAreas. You add and
//        // remove templates through the shared CodeTemplateManager instance.
//        CodeTemplateManager ctm = RSyntaxTextArea.getCodeTemplateManager();
//
//        // StaticCodeTemplates are templates that insert static text before and
//        // after the current caret position. This template is basically shorthand
//        // for "System.out.println(".
//        CodeTemplate ct = new StaticCodeTemplate("sout", "System.out.println(", null);
//        ctm.addTemplate(ct);
//
//        // This template is for a for-loop. The caret is placed at the upper
//        // bound of the loop.
//        ct = new StaticCodeTemplate("fb", "for (int i=0; i<", "; i++) {\n\t\n}\n");
//        ctm.addTemplate(ct);

//        AbstractTokenMakerFactory atmf = (AbstractTokenMakerFactory) TokenMakerFactory.getDefaultInstance();
//        atmf.putMapping("text/SDE", "application.SDETokenMaker");
//        sourceCodeTextArea.setSyntaxEditingStyle("text/SDE");

        // Auto complete
        CompletionProvider provider = createCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationDelay(1000);
        ac.setAutoActivationEnabled(true);
        ac.setShowDescWindow(true);
        ac.install(sourceCodeTextArea);

        this.setContent(cp);
    }

    private CompletionProvider createCompletionProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
//
//        BasicCompletion basicCompletion = new BasicCompletion(provider, "ugh2");
//        basicCompletion.setShortDescription("DESC");
//        provider.addCompletion(basicCompletion);

        // SSH
        // SDEUtils.openSSHSession
        FunctionCompletion functionCompletion = new FunctionCompletion(provider, "SDEUtils.openSSHSession(String connectionIP, String username, String password)", "SSHManager");
        List<ParameterizedCompletion.Parameter> params = new ArrayList<ParameterizedCompletion.Parameter>();
        params.add(new ParameterizedCompletion.Parameter("String", "connectionIP"));
        params.add(new ParameterizedCompletion.Parameter("String", "username"));
        params.add(new ParameterizedCompletion.Parameter("String", "password"));
        functionCompletion.setParams(params);
        functionCompletion.setDefinedIn("SDEUtils");
        functionCompletion.setSummary("Create a new SSH connection that can be used." +
                "<br/>connectionIP can take the form of ip:port or just ip and the port will default to 22" +
                "<br/>This will return the newly opened connection as an SSHManager");
        provider.addCompletion(functionCompletion);

        // SCREENSHOT
        // ScreenShot.takeScreenShot
        functionCompletion = new FunctionCompletion(provider, "ScreenShot.takeScreenShot(int screen)", "void");
        params = new ArrayList<ParameterizedCompletion.Parameter>();
        params.add(new ParameterizedCompletion.Parameter("int", "screen"));
        params.add(new ParameterizedCompletion.Parameter("String", "name"));
        functionCompletion.setParams(params);
        functionCompletion.setDefinedIn("ScreenShot");
        functionCompletion.setSummary("Takes a screen shot and saves it as <name>.png" +
                "<br/>Possible screen options:" +
                "<br/>PRIMARY_SCREEN, ALL_SCREENS");
        provider.addCompletion(functionCompletion);

        // PRIMARY_SCREEN
        VariableCompletion variableCompletion = new VariableCompletion(provider, "PRIMARY_SCREEN", "int");
        variableCompletion.setShortDescription("Screenshot - Primary screen is your default monitor");
        variableCompletion.setSummary("<b>BOLD, GOLD! ALWAYS BELIEVE IN YOUR SOUL!</b>");
        provider.addCompletion(variableCompletion);

        // ALL_SCREENS
        variableCompletion = new VariableCompletion(provider, "ALL_SCREENS", "int");
        variableCompletion.setShortDescription("Screenshot - Take screen shot of everything");
        variableCompletion.setSummary("<b>BOLD, GOLD! ALWAYS BELIEVE IN YOUR SOUL!</b>");
        provider.addCompletion(variableCompletion);

        return provider;
    }

//    class SourceCodeTextAreaMouseListener implements MouseListener {
//        @Override
//        public void mouseClicked(MouseEvent e) {
//            System.out.println("It was clicked");
//            //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public void mousePressed(MouseEvent e) {
//            //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public void mouseReleased(MouseEvent e) {
//            //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public void mouseEntered(MouseEvent e) {
//            //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public void mouseExited(MouseEvent e) {
//            //To change body of implemented methods use File | Settings | File Templates.
//        }
//    }

    class SourceCodeTextAreaDocumentListener implements DocumentListener {
        public void insertUpdate(DocumentEvent e) {
            updateSource();
        }

        public void removeUpdate(DocumentEvent e) {
            updateSource();
        }

        public void changedUpdate(DocumentEvent e) {
            //Plain text components do not fire these events
        }

        private void updateSource() {
            Program program = DataBank.currentlyEditProgram;
            if (program != null) {
                source.setSource(sourceCodeTextArea.getText());
                program.getFlowController().checkConnections();
            }
        }
    }
}
