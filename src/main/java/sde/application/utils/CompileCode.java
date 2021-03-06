package sde.application.utils;

import sde.application.error.Error;
import sde.application.gui.dialog.ErrorDialog;
import sde.application.node.objects.Logic;
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

    public static CompileResult compileCode(Logic logic) {
        CompileResult compileResult = new CompileResult();
        String className = "SDEClass" + logic.getUuidStringWithoutHyphen() + "C" + counter;
        counter++;
        try {
            String logicString = "";
            if (logic.getType().equals(Logic.TEST_NODE_LOGIC)) {
                logicString = getLogicNodeCode(logic, className);
            } else if (logic.getType().equals(Logic.TEST_CASE_NODE_LOGIC)) {
                logicString = getTestCaseNodeCode(logic, className);
            }

            String userHome = System.getProperty("user.home");

            // TODO: Clean up old class files when new ones are recompiled.
            // TODO: Make the class files temporary files so that they are auto cleaned up on exit.
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
                String[] lines = errString.split("\n");

                Boolean withinError = false;
                String currentErrorLineNumber = "1";
                String classFileName = className + ".java";
                for (String line : lines) {
                    line = line.replace("\r", "").replace("\n", "");
                    if (!withinError && line.contains(classFileName)) { // Find an error message by looking for the classname of the current file
                        if (line.contains(": ")) {
                            String errorLineNumber = line.substring(line.indexOf(classFileName) + classFileName.length() + 1, line.indexOf(": "));
                            String error = line.substring(line.indexOf(":" + errorLineNumber + ":") + 2 + errorLineNumber.length());
                            compileResult.addLineCompileError(Integer.parseInt(errorLineNumber), error);
                            withinError = true;
                            currentErrorLineNumber = errorLineNumber;
                        }
                    } else { // Continue adding subsequent lines as they still relate to the same error message
                        if (withinError) {
                            if (line.trim().equals("^")) { // Shows where the error is against the line above
                                withinError = false;
                                compileResult.addLineCompileError(Integer.parseInt(currentErrorLineNumber), line);
                            } else if (line.contains(className)) { // Checks for "location: <class name> for ending of the error part
                                withinError = false;
                            } else {
                                compileResult.addLineCompileError(Integer.parseInt(currentErrorLineNumber), line);
                            }
                        }
                    }
                }

                className = null;
            }
            if (outString.length() > 1) {
                Error.CODE_COMPILE.record().additionalInformation(outString).create();

                compileResult.addCompileError(outString);
            }
            out.close();
            err.close();
        } catch (Exception ex) {
            compileResult.addCompiledException(ex);
            Error.CODE_COMPILE.record().create(ex);
            className = null;
        }

        // Null classname means that the compile did not succeed.
        compileResult.setSuccessfulCompile(className != null);
        compileResult.setClassName(className);
        return compileResult;
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
                "import sde.application.utils.*;" +
                "import sde.application.utils.managers.*;" +
                "import sde.application.data.*;" +
                "import sde.application.data.export.*;" +
                "import sde.application.data.chart.*;" +
                "import java.util.*;" +
                "import sde.application.gui.*;" +
                "import sde.application.test.*;" +
                "import sde.application.test.core.*;" +
                "import sde.application.test.annotation.*;" +
                "import sde.application.error.Error;" +
                "import sde.application.net.proxy.*;" +
                "import sde.application.node.implementations.*;" +
                "import sde.application.node.design.*;" +
                "import sde.application.node.objects.*;" +
                "import sde.application.node.objects.datatable.*;" +
                "import sde.application.net.proxy.*;" +
                "import sde.application.test.action.helpers.*;" +
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
                "import sde.application.utils.*;" +
                "import sde.application.utils.managers.*;" +
                "import sde.application.data.*;" +
                "import sde.application.data.export.*;" +
                "import sde.application.data.chart.*;" +
                "import java.util.*;" +
                "import org.openqa.selenium.*;" +
                "import org.openqa.selenium.support.ui.*;" +
                "import sde.application.gui.*;" +
                "import sde.application.test.*;" +
                "import sde.application.test.core.*;" +
                "import sde.application.test.annotation.*;" +
                "import sde.application.error.Error;" +
                "import sde.application.net.proxy.*;" +
                "import sde.application.node.implementations.*;" +
                "import sde.application.node.design.*;" +
                "import sde.application.node.objects.*;" +
                "import sde.application.node.objects.datatable.*;" +
                "import sde.application.net.proxy.*;" +
                "import org.jsoup.Jsoup;" +
                "import org.jsoup.nodes.*;" +
                "import org.jsoup.select.*;" +
                "import sde.application.test.action.helpers.*;" +
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
                "       threadWait();" + // Wait for any threads that we might have joined to to finish
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
