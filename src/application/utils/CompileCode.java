package application.utils;

import application.gui.FlowController;
import application.gui.dialog.ErrorDialog;
import application.gui.dialog.ExceptionDialog;
import application.node.objects.Logic;
import javafx.scene.control.Alert;
import org.apache.log4j.Logger;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;

public class CompileCode {
    static int counter = 0;

    private static Logger log = Logger.getLogger(CompileCode.class);

    public static String compileCode(Logic logic) {
        String className = "SDEClass" + logic.getId() + "C" + counter;
        counter++;
        String flowControllerReferenceId = "[Unloaded FlowController]";
        try {
            flowControllerReferenceId = FlowController.getFlowControllerFromLogic(logic).getReferenceID();
            String logicReferenceId = logic.getId().toString();
            String logicString = "package programs;" +
                    "import application.utils.*;" +
                    "import application.utils.managers.*;" +
                    "import application.data.*;" +
                    "import application.data.export.*;" +
                    "import application.data.chart.*;" +
                    "import java.util.*;" +
                    "import org.openqa.selenium.*;" +
                    "import org.openqa.selenium.support.ui.*;" +
                    "import application.gui.*;" +
                    "import application.test.*;" +
                    "import application.net.ssh.*;" +
                    "import application.net.proxy.*;" +
                    "import application.node.implementations.*;" +
                    "import application.node.design.*;" +
                    "import application.node.objects.*;" +
                    "import application.net.proxy.*;" +
                    "import org.apache.log4j.Logger;" +
                    "public class " + className + " extends SDERunnable {" +
                    "   private String flowControllerReferenceId = \"" + flowControllerReferenceId + "\";" +
                    "   private String logicReferenceId = \"" + logicReferenceId + "\";" +
                    "   private NodeRunParams nodeRunParams = new NodeRunParams();" +
                    "   private Logger log = Logger.getLogger(\"" + logic.getParentLogicNode().getContainedText() + " (#" + logic.getParentLogicNode().getId() + ")\");" +
                    "   " + logic.getLogic() + "" +
                    "   public void threadRun() {" +
                    "      FlowController.sourceStarted(this.logicReferenceId);" +
                    "      function();" +
                    "      FlowController.sourceFinished(this.logicReferenceId);" +
                    "   }" +
                    "   public void init(NodeRunParams nodeRunParams) {" +
                    "      this.nodeRunParams = new NodeRunParams(nodeRunParams);" +
                    "   }" +
                    "   private void save(String name, Object object) {" +
                    "      DataBank.saveVariable(name, object, this.flowControllerReferenceId);" +
                    "   }" +
                    "   private Object load(String name) {" +
                    "      return DataBank.loadVariable(name, this.flowControllerReferenceId);" +
                    "   }" +
                    "   private void run(String name) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, false, false, new NodeRunParams());" +
                    "   }" +
                    "   private SSHManager ssh(String connection, String username, String password, String consoleName) {" +
                    "      return SDEUtils.openSSHSession(connection,username,password,consoleName,this.flowControllerReferenceId);" +
                    "   }" +
                    "   private void run(String name, NodeRunParams nodeRunParams) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, false, false, new NodeRunParams(nodeRunParams));" +
                    "   }" +
                    "   private void runAndWait(String name) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, true, false, new NodeRunParams());" +
                    "   }" +
                    "   private void runAndWait(String name, NodeRunParams nodeRunParams) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, true, false, new NodeRunParams(nodeRunParams));" +
                    "   }" +
                    "   private DrawableNode getNode(String name) {" +
                    "      return FlowController.getFlowControllerFromReference(this.flowControllerReferenceId).getNodeThisControllerFromContainedText(name);" +
                    "   }" +
                    "}";

            String userHome = System.getProperty("user.home");

            // Save source in .java file.
            File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
            File sourceFile = new File(root, "programs/" + className + ".java");
            Boolean mkDirResult = sourceFile.getParentFile().mkdirs();

            new FileWriter(sourceFile).append(logicString).close();
            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            compiler.run(null, out, err, sourceFile.getPath());

            String outString = new String(out.toByteArray(), Charset.defaultCharset());
            String errString = new String(err.toByteArray(), Charset.defaultCharset());

            if (errString.length() > 1) {
                String lineNumber = errString.substring(errString.indexOf(className) + className.length() + 6);
                if (lineNumber.contains(":")) {
                    lineNumber = lineNumber.substring(0, lineNumber.indexOf(":"));
                }
                log.info("Error compiling " + logic.getParentLogicNode().getContainedText() + " - " + lineNumber + " - " + errString);

                new ErrorDialog()
                        .content(errString)
                        .title("Compile error on " + logic.getParentLogicNode().getContainedText())
                        .header("Error at line " + lineNumber)
                        .show();
                className = null;
            }
            if (outString.length() > 1) {
                log.info("Error compiling " + outString);

                new ErrorDialog()
                        .content(outString)
                        .header("Compile error on " + logic.getParentLogicNode().getContainedText())
                        .show();

                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("ERR");
                errorAlert.setContentText(outString);
                errorAlert.show();
            }
            out.close();
            err.close();
        } catch (Exception ex) {
            new ExceptionDialog()
                    .title("Compile Error")
                    .content("Exception encountered while trying to compile " + flowControllerReferenceId)
                    .exception(ex)
                    .show();
            log.error("Error compiling code", ex);
            className = null;
        }

        // Returning null classname means that the compile did not succeed.
        return className;
    }
}
