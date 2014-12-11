package application.gui;

import application.data.DataBank;
import application.node.implementations.BashNode;
import application.utils.NodeRunParams;

public class Bash {
    private String script;
    private BashNode parentBashNode;
    private Integer id = -1;

    public Bash(BashNode parentBashNode) {
        this.parentBashNode = parentBashNode;
        this.script = "#!/bin/bash\n" +
                "VAR=\"hello\"\n" +
                "echo $VAR";
    }

    public Bash(BashNode parentBashNode, String script, Integer id) {
        this.parentBashNode = parentBashNode;
        this.script = script;
        this.id = id;
    }

    public BashNode getParentBashNode() {
        return this.parentBashNode;
    }

    public String getScript() {
        return this.script;
    }

    public void setScript(String script) {
        if (!this.script.equals(script)) {
            this.script = script;
            if (!parentBashNode.isInitialising()) {
                DataBank.saveNode(parentBashNode);
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void run(Boolean whileWaiting, NodeRunParams nodeRunParams) {
//        if (!this.compiled) {
//            compile();
//        }
//        if (this.compiled) {
//            Object instance = null;
//            try {
//                String userHome = System.getProperty("user.home");
//                File root = new File(userHome, "/SDE");
//
//                URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{root.toURI().toURL()});
//                Class<?> cls = Class.forName("programs." + compiledClassName, true, classLoader);
//                instance = cls.newInstance();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//
//            if (instance != null) {
//                Method method;
//                try {
//                    method = instance.getClass().getMethod("init", HashMap.class);
//                    method.invoke(instance, map);
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//
//                new SDEThread((SDERunnable) instance);
////                if (whileWaiting) {
////                    try {
////                        t.join();
////                    } catch (InterruptedException e) {
////                        e.printStackTrace();
////                    }
////                }
//            }
//        }
    }
}
