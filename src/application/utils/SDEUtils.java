package application.utils;

import application.gui.FlowController;
import application.net.SSHManager;
import application.node.ConsoleNode;
import application.node.DrawableNode;
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

    public static SSHManager openSSHSession(String connection, String username, String password, String consoleName, String flowControllerReferenceId) {
        JSch.setConfig("StrictHostKeyChecking", "no");
        SSHManager instance;

        String[] connectionInfo = connection.split(":");
        if (connectionInfo.length > 1) {
            instance = new SSHManager(username, password, connectionInfo[0], knownHosts, Integer.parseInt(connectionInfo[1]));
        } else {
            instance = new SSHManager(username, password, connectionInfo[0], knownHosts, 22);
        }

        if (consoleName != null) {
            FlowController flowController = FlowController.getFlowControllerFromReference(flowControllerReferenceId);
            if (flowController != null) {
                for (DrawableNode consoleNode : flowController.getNodes()) {
                    if (consoleNode.getContainedText().equals(consoleName)) {
                        if (consoleNode instanceof ConsoleNode) {
                            instance.setConsoleNode((ConsoleNode) consoleNode);
                        }
                    }
                }
            }
        }

        String errorMessage = instance.connect();
        instance.createShellChannel();

        if (errorMessage != null) {
            System.out.println("ERROR " + errorMessage);
        }

        return instance;
    }

    public static void editFileReplaceText(SSHManager sshManager, String find, String replace, String fileLocation) {
        find = find.replace("/", "\\/");
        replace = replace.replace("/", "\\/");
        // sed with in place replacement (does not create new file, this is the -i flag)
        sshManager.sendShellCommand("sed -i 's/" + find + "/" + replace + "/' " + fileLocation);
    }

    public static void runCMDCommand(String command) {
        try {
            // Execute command
            Process child = Runtime.getRuntime().exec("cmd /C " + command);

            InputStream is = child.getInputStream();
            InputStream errSt = child.getErrorStream();
            int in = -1;

            while ((in = is.read()) != -1) {
                System.out.print(((char) in));
            }
            while ((in = errSt.read()) != -1) {
                System.out.print(((char) in));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public static void takeDatabaseDump(SSHManager sshManager, String user, String password, String schema, String databaseType, Boolean zipFile) {
        String command = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMdd_HHmmss");
        String dateFormat = sdf.format(new Date());
        String fileName = "";
        if ("ORACLE".equals(databaseType)) {
            fileName = dateFormat + "_" + user;
            command = "expdp " + user + "/" + password + " schemas=" + schema + " directory=dmptmp dumpfile=" + fileName + ".dmp logfile=" + fileName + ".log";
        }
        if (!"".equals(command)) {
            sshManager.sendShellCommand(command);
            if (zipFile) {
                sshManager.sendShellCommand("gzip -9 " + fileName + ".dmp");
            }
        }
    }
}
