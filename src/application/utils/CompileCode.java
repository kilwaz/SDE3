package application.utils;

import application.error.Error;
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
        String className = "SDEClass" + logic.getUuidStringWithoutHyphen() + "C" + counter;
        counter++;
        String flowControllerReferenceId = "[Unloaded FlowController]";
        try {
            String logicString = "";
            if (logic.getType().equals(Logic.TEST_NODE_LOGIC)) {
                logicString = getLogicNodeCode(logic, className);
            } else if (logic.getType().equals(Logic.TEST_CASE_NODE_LOGIC)) {
                logicString = getTestCaseNodeCode(logic, className);
            }

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

            if (compiler != null) {
                compiler.run(null, out, err, sourceFile.getPath());
            } else {
                new ErrorDialog()
                        .content("Make sure you are running from a full JDK and not just a JRE")
                        .title("Compiler not found")
                        .show();
            }

            String outString = new String(out.toByteArray(), Charset.defaultCharset());
            String errString = new String(err.toByteArray(), Charset.defaultCharset());

            if (errString.length() > 1) {
                String lineNumber = errString.substring(errString.indexOf(className) + className.length() + 6);
                if (lineNumber.contains(":")) {
                    lineNumber = lineNumber.substring(0, lineNumber.indexOf(":"));
                }
                Error.CODE_COMPILE.record().additionalInformation(logic.getParentLogicNode().getContainedText() + " - " + lineNumber + " - " + errString).create();
                new ErrorDialog()
                        .content(errString)
                        .title("Compile error on " + logic.getParentLogicNode().getContainedText())
                        .header("Error at line " + lineNumber)
                        .show();
                className = null;
            }
            if (outString.length() > 1) {
                Error.CODE_COMPILE.record().additionalInformation(outString).create();

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
            Error.CODE_COMPILE.record().create(ex);
            className = null;
        }

        // Returning null classname means that the compile did not succeed.
        return className;
    }

    private static String getTestCaseNodeCode(Logic logic, String className) {
        String flowControllerReferenceId = logic.getParentLogicNode().getProgram().getFlowController().getReferenceID();
        String logicReferenceUuid = logic.getParentLogicNode().getUuidString();
        String logicSource = logic.getLogic();

        if (!logicSource.contains("// METHOD ANNOTATIONS")) {
            Error.TEST_CASE_METHOD_ANNOTATIONS_NOT_FOUND.record().create();
            return "";
        }

        String classAnnotations = logicSource.substring(0, logicSource.indexOf("// METHOD ANNOTATIONS"));
        String methodAnnotations = logicSource.substring(logicSource.indexOf("// METHOD ANNOTATIONS"));
        return "package programs;" +
                "import application.utils.*;" +
                "import application.utils.managers.*;" +
                "import application.data.*;" +
                "import application.data.export.*;" +
                "import application.data.chart.*;" +
                "import java.util.*;" +
                "import application.gui.*;" +
                "import application.test.*;" +
                "import application.test.core.*;" +
                "import application.test.annotation.*;" +
                "import application.error.Error;" +
                "import application.net.proxy.*;" +
                "import application.node.implementations.*;" +
                "import application.node.design.*;" +
                "import application.node.objects.*;" +
                "import application.node.objects.datatable.*;" +
                "import application.net.proxy.*;" +
                "import application.test.action.helpers.*;" +
                "import org.jsoup.Jsoup;" +
                "import org.jsoup.nodes.*;" +
                "import org.jsoup.select.*;" +
                "import org.apache.log4j.Logger;" +
                "@SuppressWarnings(\"unchecked\")" +
                classAnnotations + "\r\n" +
                "public class " + className + " extends TestTemplate {" +
                "   private String flowControllerReferenceId = \"" + flowControllerReferenceId + "\";" +
                "   private String logicReferenceUuid = \"" + logicReferenceUuid + "\";" +
                "   private NodeRunParams nodeRunParams = new NodeRunParams();" +
                "   private Logger log = Logger.getLogger(\"" + logic.getParentLogicNode().getContainedText() + " (#" + logic.getParentLogicNode().getUuidStringWithoutHyphen() + ")\");" +
                "   @SuppressWarnings({\"unchecked\",\"deprecation\"})" +
                "   public " + className + "() {" +
                "        super();" +
                "    }" +
                "   " + methodAnnotations + "\r\n" +
                "   private void run(String name) {" +
                "      Program.runHelper(name, this.flowControllerReferenceId, null, false, true, \"" + className + "\", new NodeRunParams());" +
                "   }" +
                "   public void threadWait() {" +
                "      try {" +
                "           SDEThreadCollection sdeThreadCollection = ThreadManager.getInstance().getThreadCollection(\"" + className + "\");" +
                "           if(sdeThreadCollection != null) {" +
                "               sdeThreadCollection.join();" +
                "           }" +
                "      } catch (Exception ex) {" +
                "           Error.COMPILED_LOGIC_NODE.record().additionalInformation(\"Node - " + logic.getParentLogicNode().getContainedText() + " (" + className + ")\").create(ex);" +
                "      }" +
                "   }" +
                "   private void run(String name, NodeRunParams nodeRunParams) {" +
                "      Program.runHelper(name, this.flowControllerReferenceId, null, false, true, \"" + className + "\", new NodeRunParams(nodeRunParams));" +
                "   }" +
                "   private void runAndWait(String name) {" +
                "      Program.runHelper(name, this.flowControllerReferenceId, null, true, true, \"" + className + "\", new NodeRunParams());" +
                "   }" +
                "   private void runAndWait(String name, NodeRunParams nodeRunParams) {" +
                "      Program.runHelper(name, this.flowControllerReferenceId, null, true, true, \"" + className + "\", new NodeRunParams(nodeRunParams));" +
                "   }" +
                "   private void runAndJoin(String name) {" +
                "      SDEThread sdeThread = Program.runHelper(name, this.flowControllerReferenceId, null, false, true, \"" + className + "\", new NodeRunParams());" +
                "   }" +
                "   private void runAndJoin(String name, NodeRunParams nodeRunParams) {" +
                "      SDEThread sdeThread = Program.runHelper(name, this.flowControllerReferenceId, null, false, true, \"" + className + "\", new NodeRunParams(nodeRunParams));" +
                "   }" +
                "   private DrawableNode getNode(String name) {" +
                "      return FlowController.getFlowControllerFromReference(this.flowControllerReferenceId).getNodeThisControllerFromContainedText(name);" +
                "   }" +
                "}";
    }

    private static String getLogicNodeCode(Logic logic, String className) {
        String flowControllerReferenceId = logic.getParentLogicNode().getProgram().getFlowController().getReferenceID();
        String logicReferenceUuid = logic.getParentLogicNode().getUuidString();
        return "package programs;" +
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
                "import application.test.core.*;" +
                "import application.test.annotation.*;" +
                "import application.error.Error;" +
                "import application.net.proxy.*;" +
                "import application.node.implementations.*;" +
                "import application.node.design.*;" +
                "import application.node.objects.*;" +
                "import application.node.objects.datatable.*;" +
                "import application.net.proxy.*;" +
                "import org.jsoup.Jsoup;" +
                "import org.jsoup.nodes.*;" +
                "import org.jsoup.select.*;" +
                "import application.test.action.helpers.*;" +
                "import org.apache.log4j.Logger;" +
                "@SuppressWarnings(\"unchecked\")" +
                "public class " + className + " extends SDERunnable {" +
                "   private String flowControllerReferenceId = \"" + flowControllerReferenceId + "\";" +
                "   private String logicReferenceUuid = \"" + logicReferenceUuid + "\";" +
                "   private NodeRunParams nodeRunParams = new NodeRunParams();" +
                "   private Logger log = Logger.getLogger(\"" + logic.getParentLogicNode().getContainedText() + " (#" + logic.getParentLogicNode().getUuidStringWithoutHyphen() + ")\");" +
                "   " + logic.getLogic() + "\r\n" +
                "   public void threadRun() {" +
                "       FlowController.sourceStarted(this.logicReferenceUuid);" +
                "       function();" +
                "       threadWait();" +
                "       FlowController.sourceFinished(this.logicReferenceUuid);" +
                "   }" +
                "   public void threadWait() {" +
                "      try {" +
                "           SDEThreadCollection sdeThreadCollection = ThreadManager.getInstance().getThreadCollection(\"" + className + "\");" +
                "           if(sdeThreadCollection != null) {" +
                "               sdeThreadCollection.join();" +
                "           }" +
                "      } catch (Exception ex) {" +
                "           Error.COMPILED_LOGIC_NODE.record().additionalInformation(\"Node - " + logic.getParentLogicNode().getContainedText() + " (" + className + ")\").create(ex);" +
                "      }" +
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
                "   private void runAndJoin(String name) {" +
                "      SDEThread sdeThread = Program.runHelper(name, this.flowControllerReferenceId, null, false, true, \"" + className + "\", new NodeRunParams());" +
                "   }" +
                "   private void runAndJoin(String name, NodeRunParams nodeRunParams) {" +
                "      SDEThread sdeThread = Program.runHelper(name, this.flowControllerReferenceId, null, false, true, \"" + className + "\", new NodeRunParams(nodeRunParams));" +
                "   }" +
                "   private void run(String name) {" +
                "      Program.runHelper(name, this.flowControllerReferenceId, null, false, true, null, new NodeRunParams());" +
                "   }" +
                "   private void run(String name, NodeRunParams nodeRunParams) {" +
                "      Program.runHelper(name, this.flowControllerReferenceId, null, false, true, null, new NodeRunParams(nodeRunParams));" +
                "   }" +
                "   private void runAndWait(String name) {" +
                "      SDEThread sdeThread = Program.runHelper(name, this.flowControllerReferenceId, null, true, true, null, new NodeRunParams());" +
                "   }" +
                "   private void runAndWait(String name, NodeRunParams nodeRunParams) {" +
                "      Program.runHelper(name, this.flowControllerReferenceId, null, true, true, null, new NodeRunParams(nodeRunParams));" +
                "   }" +
                "   private DrawableNode getNode(String name) {" +
                "      return FlowController.getFlowControllerFromReference(this.flowControllerReferenceId).getNodeThisControllerFromContainedText(name);" +
                "   }" +
                "}";
    }
}
