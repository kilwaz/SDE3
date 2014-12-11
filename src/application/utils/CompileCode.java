package application.utils;

import application.gui.Controller;
import application.gui.FlowController;
import application.gui.Source;
import org.controlsfx.dialog.Dialogs;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;

public class CompileCode {
    static int counter = 0;

    public static String compileCode(Source source) {
        String className = "SDEClass" + source.getId() + "C" + counter;
        String flowControllerReferenceId = "[Unloaded FlowController]";
        try {
            flowControllerReferenceId = FlowController.getFlowControllerFromSource(source).getReferenceID();
            String sourceReferenceId = source.getId().toString();
            String sourceString = "package programs;" +
                    "import application.utils.*;" +
                    "import application.data.*;" +
                    "import java.util.*;" +
                    "import org.openqa.selenium.*;" +
                    "import org.openqa.selenium.support.ui.*;" +
                    "import application.gui.*;" +
                    "import application.test.*;" +
                    "import application.net.*;" +
                    "import application.node.*;" +
                    "public class " + className + " extends SDERunnable {" +
                    "   private String flowControllerReferenceId = \"" + flowControllerReferenceId + "\";" +
                    "   private String sourceReferenceId = \"" + sourceReferenceId + "\";" +
                    "   private HashMap<String, Object> params;" +
                    "" + source.getSource() + "" +
                    "   public void threadRun() {" +
                    "      FlowController.sourceStarted(this.sourceReferenceId);" +
                    "      function();" +
                    "      FlowController.sourceFinished(this.sourceReferenceId);" +
                    "   }" +
                    "   public void init(HashMap<String, Object> params) {" +
                    "      this.params = params;" +
                    "   }" +
                    "   private void save(String name, Object object) {" +
                    "      DataBank.saveVariable(name, object, this.flowControllerReferenceId);" +
                    "   }" +
                    "   private Object load(String name) {" +
                    "      return DataBank.loadVariable(name, this.flowControllerReferenceId);" +
                    "   }" +
                    "   private void recordResult(TestResult testResult, String name) {" +
                    "      TestHelper.getResultSet(name, this.flowControllerReferenceId).addResult(testResult);" +
                    "   }" +
                    "   private void run(String name) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, false, false, new HashMap<String, Object>());" +
                    "   }" +
                    "   private SSHManager ssh(String connection, String username, String password, String consoleName) {" +
                    "      return SDEUtils.openSSHSession(connection,username,password,consoleName,this.flowControllerReferenceId);" +
                    "   }" +
                    "   private void run(String name, HashMap<String, Object> map) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, false, false, map);" +
                    "   }" +
                    "   private void runAndWait(String name) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, true, false, new HashMap<String, Object>());" +
                    "   }" +
                    "   private void runAndWait(String name, HashMap<String, Object> map) {" +
                    "      Program.runHelper(name, this.flowControllerReferenceId, null, true, false, map);" +
                    "   }" +
                    "   private DrawableNode getNode(String name) {" +
                    "      return FlowController.getFlowControllerFromReference(this.flowControllerReferenceId).getNodeThisControllerFromContainedText(name);" +
                    "   }" +
                    "}";

            String userHome = System.getProperty("user.home");

            // Save source in .java file.
            File root = new File(userHome, "/SDE"); // On Windows running on C:\, this is C:\java.
            //File root = new File("C:\\developers\\alex\\svnwork\\focal-v6-demo-test\\SDE\\out\\production\\SDE\\programs"); // On Windows running on C:\, this is C:\java.
            File sourceFile = new File(root, "programs/" + className + ".java");
            sourceFile.getParentFile().mkdirs();

            new FileWriter(sourceFile).append(sourceString).close();
            // Compile source file.
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();

            compiler.run(null, out, err, sourceFile.getPath());

            String outString = new String(out.toByteArray(), Charset.defaultCharset());
            String errString = new String(err.toByteArray(), Charset.defaultCharset());

            if (errString.length() > 1) {
                String lineNumber = errString.substring(errString.indexOf(className) + className.length() + 6);
                lineNumber = lineNumber.substring(0, lineNumber.indexOf(":"));
                Controller.getInstance().showError(Dialogs.create()
                        .owner(null)
                        .title("Compile error on " + source.getParentSourceNode().getContainedText())
                        .masthead("Error at line " + lineNumber)
                        .message(errString));
                className = null;
            }
            if (outString.length() > 1) {
                Dialogs.create()
                        .owner(null)
                        .title("ERR")
                        .masthead(null)
                        .message(outString)
                        .showError();
            }
            out.close();
            err.close();
        } catch (Exception ex) {
            Controller.getInstance().showException(Dialogs.create()
                    .owner(null)
                    .title("Compile Error")
                    .masthead(null)
                    .message("Exception encountered while trying to compile " + flowControllerReferenceId), ex);
            className = null;
        }
        counter++;

        // Returning null classname means that the compile did not succeed.
        return className;
    }
}
