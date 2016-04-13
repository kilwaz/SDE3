package application.node.objects;

import application.error.Error;
import application.gui.Program;
import application.node.design.DrawableNode;
import application.node.implementations.LogicNode;
import application.utils.CompileCode;
import application.utils.NodeRunParams;
import application.utils.SDERunnable;
import application.utils.SDEThread;
import org.apache.log4j.Logger;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class Logic {
    public static final Integer UNKNOWN_LOGIC = -1;
    public static final Integer TEST_NODE_LOGIC = 0;
    public static final Integer TEST_CASE_NODE_LOGIC = 1;
    private static Logger log = Logger.getLogger(Logic.class);
    private Boolean compiled = false;
    private String logic;
    private Object compiledInstance;
    private DrawableNode parentLogicNode;
    private String compiledClassName = "UNKNOWN";
    private Integer type = UNKNOWN_LOGIC;

    public Logic(DrawableNode parentLogicNode) {
        this.parentLogicNode = parentLogicNode;
        if (parentLogicNode instanceof LogicNode) {
            this.logic = "public void function() {\n" +
                    "   log.info(\"Sample code\");\n" +
                    "}";
            this.type = TEST_NODE_LOGIC;
        } else {
            this.logic = "// CLASS ANNOTATIONS\n" +
                    "@TestNodes(testList = {\"Test1\"})\n" +
                    "\n" +
                    "@TestInput(name=\"name\", val=\"value\")\n" +
                    "\n" +
                    "// METHOD ANNOTATIONS\n" +
                    "// Values\n" +
                    "@AssertChange(id=\"id\",type=\"type\",attribute=\"attribute\")\n" +
                    "public void method(AssertData assertData){\n" +
                    "    \n" +
                    "}\n" +
                    "\n" +
                    "@OnComplete\n" +
                    "public void onComplete(OnCompleteData onCompleteData) {\n" +
                    "    nodeRunParams.setOneTimeVariable(onCompleteData.testCase());\n" +
                    "    runAndWait(\"Do Something\",nodeRunParams);\n" +
                    "}";
            this.type = TEST_CASE_NODE_LOGIC;
        }
    }

    public Logic(DrawableNode parentLogicNode, String logic) {
        this.parentLogicNode = parentLogicNode;
        this.logic = logic;
    }

    public DrawableNode getParentLogicNode() {
        return this.parentLogicNode;
    }

    public String getLogic() {
        return this.logic;
    }

    public void setLogic(String logic) {
        if (!this.logic.equals(logic)) {
            this.compiled = false;
            this.logic = logic;
            if (!parentLogicNode.isInitialising()) {
                parentLogicNode.save();
            }

            Program program = getParentLogicNode().getProgram();
            if (program != null) {
                program.getFlowController().checkConnections();
            }
        }
    }

    public String getUuidStringWithoutHyphen() {
        return parentLogicNode.getUuidStringWithoutHyphen();
    }

    public Boolean isCompiled() {
        return this.compiled;
    }

    public Object getCompiledInstance() {
        return this.compiledInstance;
    }

    public Boolean compile() {
        this.compiledInstance = null;
        this.compiled = false;
        String className = CompileCode.compileCode(this);
        if (className != null) {
            this.compiled = true;
            this.compiledClassName = className;
            return true;
        }
        return false;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams, String threadReference) {
        Object instance = getObjectInstance();
        if (instance != null) {
            if (type.equals(TEST_NODE_LOGIC)) {
                Method method;
                try {
                    method = instance.getClass().getMethod("init", NodeRunParams.class);
                    method.invoke(instance, nodeRunParams);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    Error.RUN_LOGIC_NODE_INIT.record().additionalInformation("Class" + compiledClassName).create(ex);
                }

                SDEThread sdeThread = new SDEThread((SDERunnable) instance, "Running logic for - " + this.getParentLogicNode().getContainedText(), threadReference, true);
                if (whileWaiting) {
//                    log.info("THREADING:Starting to wait for logic '" + sdeThread.getDescription() + "' to finish");
                    sdeThread.join();
//                    log.info("THREADING:Logic '" + sdeThread.getDescription() + "' has finished");
                }
            }
        }
    }

    public Object getObjectInstance() {
        if (!this.compiled) {
            compile();
        }
        Object instance = null;
        try {
            String userHome = System.getProperty("user.home");
            File root = new File(userHome, "/SDE");

            URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
            Class<?> cls = Class.forName("programs." + compiledClassName, true, classLoader);
            instance = cls.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | MalformedURLException ex) {
            Error.RUN_LOGIC_NODE_NEW_INSTANCE.record().additionalInformation("Class" + compiledClassName).create(ex);
        }
        return instance;
    }

    public Integer getType() {
        return type;
    }
}
