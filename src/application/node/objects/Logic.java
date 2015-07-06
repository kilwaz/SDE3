package application.node.objects;

import application.data.DataBank;
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

public class Logic {
    private Boolean compiled = false;
    private String logic;
    private Object compiledInstance;
    private LogicNode parentLogicNode;
    private Integer id = -1;
    private String compiledClassName = "UNKNOWN";

    private static Logger log = Logger.getLogger(Logic.class);

    public Logic(LogicNode parentLogicNode) {
        this.parentLogicNode = parentLogicNode;
        this.logic = "public void function() {\n" +
                "   log.info(\"Sample code\");\n" +
                "}";
    }

    public Logic(LogicNode parentLogicNode, String logic, Integer id) {
        this.parentLogicNode = parentLogicNode;
        this.logic = logic;
        this.id = id;
    }

    public LogicNode getParentLogicNode() {
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
                DataBank.saveNode(parentLogicNode);
            }

            Program program = DataBank.currentlyEditProgram;
            if (program != null) {
                program.getFlowController().checkConnections();
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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
                log.error(ex);
            }

            if (instance != null) {
                Method method;
                try {
                    method = instance.getClass().getMethod("init", NodeRunParams.class);
                    method.invoke(instance, nodeRunParams);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
                    log.error(ex);
                }

                new SDEThread((SDERunnable) instance);
            }
        }
    }
}
