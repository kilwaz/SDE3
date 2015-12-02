package application.node.objects;

import application.data.DataBank;
import application.error.Error;
import application.gui.Program;
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
import java.util.UUID;

public class Logic {
    private Boolean compiled = false;
    private String logic;
    private Object compiledInstance;
    private LogicNode parentLogicNode;
    private String compiledClassName = "UNKNOWN";

    private static Logger log = Logger.getLogger(Logic.class);

    public Logic(LogicNode parentLogicNode) {
        this.parentLogicNode = parentLogicNode;
        this.logic = "public void function() {\n" +
                "   log.info(\"Sample code\");\n" +
                "}";
    }

    public Logic(LogicNode parentLogicNode, String logic, UUID uuid) {
        this.parentLogicNode = parentLogicNode;
        this.logic = logic;
        //this.uuid = uuid;
    }

    public LogicNode getParentLogicNode() {
        return this.parentLogicNode;
    }

    public String getLogic() {
        return this.logic;
    }

    public String getUuidStringWithoutHyphen() {
        return parentLogicNode.getUuidStringWithoutHyphen();
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

//    public UUID getUuid() {
//        return uuid;
//    }
//
//    public void setUuid(UUID uuid) {
//        this.uuid = uuid;
//    }

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

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
        if (!this.compiled) {
            compile();
        }
        if (this.compiled) {
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

            if (instance != null) {
                Method method;
                try {
                    method = instance.getClass().getMethod("init", NodeRunParams.class);
                    method.invoke(instance, nodeRunParams);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    Error.RUN_LOGIC_NODE_INIT.record().additionalInformation("Class" + compiledClassName).create(ex);
                }

                new SDEThread((SDERunnable) instance, "Running logic for - " + this.getParentLogicNode().getContainedText());
            }
        }
    }
}
