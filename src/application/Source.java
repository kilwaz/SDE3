package application;

import application.utils.CompileCode;
import application.utils.DataBank;
import application.utils.ThreadManager;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

public class Source {
    private Boolean compiled = false;
    private String source;
    private Object compiledInstance;
    private SourceNode parentSourceNode;
    private Integer id = -1;
    private String compiledClassName = "UNKNOWN";

    Source(SourceNode parentSourceNode) {
        this.parentSourceNode = parentSourceNode;
        this.source = "public void function() {\n" +
                "   System.out.println(\"Sample code\");\n" +
                "}";
    }

    Source(SourceNode parentSourceNode, String source, Integer id) {
        this.parentSourceNode = parentSourceNode;
        this.source = source;
        this.id = id;
    }

    public SourceNode getParentSourceNode() {
        return this.parentSourceNode;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        if (!this.source.equals(source)) {
            this.compiled = false;
            this.source = source;
            DataBank.saveNode(parentSourceNode);

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

    public void run(Boolean whileWaiting, HashMap<String, Object> map) {
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
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            if (instance != null) {
                Method method;
                try {
                    method = instance.getClass().getMethod("init", HashMap.class);
                    method.invoke(instance, map);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                Thread t = new Thread((Runnable) instance);
                ThreadManager.getInstance().addThread(t);
                t.start();
                if (whileWaiting) {
                    try {
                        t.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
