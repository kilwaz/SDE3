package application.utils;

import application.data.OracleConnectionManager;
import application.gui.FlowController;
import application.net.SSHCommand;
import application.net.SSHManager;
import application.node.design.DrawableNode;
import application.node.implementations.ConsoleNode;
import application.node.implementations.LinuxNode;
import com.jcraft.jsch.JSch;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SDEUtils {
    static {
        try {
            knownHosts = new File(System.getProperty("user.home"), ".ssh/known_hosts").getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String knownHosts;

    // This is called from LogicNode code when creating a new SSH connection.
    public static SSHManager openSSHSession(String connection, String username, String password, String nodeName, String flowControllerReferenceId) {
        if (nodeName != null) {
            FlowController flowController = FlowController.getFlowControllerFromReference(flowControllerReferenceId);
            if (flowController != null) {
                for (DrawableNode consoleNode : flowController.getNodes()) {
                    if (consoleNode.getContainedText().equals(nodeName)) {
                        if (consoleNode instanceof ConsoleNode || consoleNode instanceof LinuxNode) {
                            return openSSHSession(connection, username, password, consoleNode);
                        }
                    }
                }
            }
        }

        return null;
    }

    // This is currently called from LinuxNode when it opens a new connection
    public static SSHManager openSSHSession(String connection, String username, String password, DrawableNode drawableNode) {
        JSch.setConfig("StrictHostKeyChecking", "no");
        SSHManager instance;

        String[] connectionInfo = connection.split(":");
        if (connectionInfo.length > 1) {
            instance = new SSHManager(username, password, connectionInfo[0], knownHosts, Integer.parseInt(connectionInfo[1]));
        } else {
            instance = new SSHManager(username, password, connectionInfo[0], knownHosts, 22);
        }


        String errorMessage = instance.connect();
        instance.createShellChannel();
        instance.setDrawableNode(drawableNode);

        if (errorMessage != null) {
            System.out.println("ERROR " + errorMessage);
        }

        return instance;
    }

    public static void editFileReplaceText(SSHManager sshManager, String find, String replace, String fileLocation) {
        find = find.replace("/", "\\/");
        replace = replace.replace("/", "\\/");
        // sed with in place replacement (does not create new file, this is the -i flag)
        sshManager.runSSHCommand(new SSHCommand("sed -i 's/" + find + "/" + replace + "/' " + fileLocation, "]$", 1000));
    }

    public static String runCMDCommand(String command) {
        StringBuilder returnString = new StringBuilder();
        returnString.append("");
        try {
            // Execute command
            Process child = Runtime.getRuntime().exec("cmd /C " + command);
            InputStream is = child.getInputStream();
            InputStream errSt = child.getErrorStream();

            int in = -1;

            while ((in = is.read()) != -1) {
                //System.out.print(((char) in));
                returnString.append((char) in);
            }
            while ((in = errSt.read()) != -1) {
                //System.out.print(((char) in));
                returnString.append((char) in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return returnString.toString();
    }

    public static void svnUpdate(String targetDirectory, Integer revision) {
        runCMDCommand("svn update -r" + revision + " " + targetDirectory);
    }

    public static void svnCheckout(String branch, String targetDirectory) {
        // https://ibis.spl.com/svn/focal-v4/branches/spl-demo-v6/
        runCMDCommand("svn checkout " + branch + " " + targetDirectory);
    }

    public static void buildApplication(String projectLocation, String project, String outputDirectory) {
        try {
            String path = projectLocation + File.separator +
                    "focal-v6-app" + File.separator +
                    "src" + File.separator +
                    "main" + File.separator +
                    "resources" + File.separator +
                    "server-config.properties";
            //(use relative path for Unix systems)
            File f = new File(path);
            if (f.exists()) {
                f.delete();
            }
            //(works for both Windows and Linux)
            f.createNewFile();

            PrintWriter fileOut = new PrintWriter(f);
            fileOut.println("JBOSS-OUTPUT-DIR=" + outputDirectory);
            fileOut.println("client-name=" + project);
            fileOut.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        runCMDCommand("cd " + projectLocation + "\\app-builder & mvn -Dclient-name=" + project + " clean package");
    }

    public static void dropDatabaseUser(SSHManager sshManager, String user) {
        OracleConnectionManager.getInstance().runQuery("drop user " + user + " cascade");

        //sshManager.runSSHCommand(new SSHCommand("sqlplus / as sysdba", "SQL>", 1000));
        //sshManager.runSSHCommand(new SSHCommand("drop user " + user + " cascade;", "SQL>", 1000));
        //sshManager.runSSHCommand(new SSHCommand("exit", "]$", 1000));
    }

    public static void createDatabaseUser(SSHManager sshManager, String user, String password) {
        OracleConnectionManager.getInstance().runQuery("create user " + user + " identified by " + password);
        OracleConnectionManager.getInstance().runQuery("grant resource,dba,connect to " + user);

        //sshManager.runSSHCommand(new SSHCommand("sqlplus / as sysdba", "SQL>", 1000));
        //sshManager.runSSHCommand(new SSHCommand("create user " + user + " identified by " + password + ";", "", 1000));
        //sshManager.runSSHCommand(new SSHCommand("grant resource,dba,connect to " + user + ";", "", 1000));
        //sshManager.runSSHCommand(new SSHCommand("exit", "]$", 1000));
    }

    public static void importDatabase(SSHManager sshManager, String user, String password, String dumpSchema, String dumpFile, String databaseType) {
        String command = "";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMdd_HHmmss");
        String dateFormat = sdf.format(new Date());
        String fileName = "";
        if ("ORACLE".equals(databaseType)) {
            fileName = dateFormat + "_" + user;
            command = "impdp " + user + "/" + password + " directory=dmptmp dumpfile=" + dumpFile + " logfile=" + fileName + ".log remap_schema=" + dumpSchema + ":" + user;
        }
        if (!"".equals(command)) {
            sshManager.runSSHCommand(new SSHCommand(command, "]$", 1000));
        }
    }

    public static void exportDatabase(SSHManager sshManager, String user, String password, String schema, String databaseType, Boolean zipFile) {
        String command = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMdd_HHmmss");
        String dateFormat = sdf.format(new Date());
        String fileName = "";
        if ("ORACLE".equals(databaseType)) {
            fileName = dateFormat + "_" + user;
            command = "expdp " + user + "/" + password + " schemas=" + schema + " directory=dmptmp dumpfile=" + fileName + ".dmp logfile=" + fileName + ".log";
        }
        if (!"".equals(command)) {
            sshManager.runSSHCommand(new SSHCommand(command, "]$", 1000));
            if (zipFile) {
                sshManager.runSSHCommand(new SSHCommand("gzip -9 " + fileName + ".dmp", "]$", 1000));
            }
        }
    }
}
