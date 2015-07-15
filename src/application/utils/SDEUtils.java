package application.utils;

import application.Main;
import application.gui.FlowController;
import application.net.ssh.SSHCommand;
import application.net.ssh.SSHManager;
import application.node.design.DrawableNode;
import application.node.implementations.ConsoleNode;
import application.node.implementations.LinuxNode;
import com.jcraft.jsch.JSch;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URLDecoder;

public class SDEUtils {
    private static Logger log = Logger.getLogger(SDEUtils.class);

    static {
        try {
            knownHosts = new File(System.getProperty("user.home"), ".ssh/known_hosts").getCanonicalPath();
        } catch (IOException ex) {
            log.error(ex);
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
            log.info("ERROR " + errorMessage);
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
                returnString.append((char) in);
            }
            while ((in = errSt.read()) != -1) {
                returnString.append((char) in);
            }
        } catch (IOException ex) {
            log.error(ex);
        }

        return returnString.toString();
    }

    public static void svnUpdate(String targetDirectory, Integer revision) {
        runCMDCommand("svn update -r" + revision + " " + targetDirectory);
    }

    public static void svnCheckout(String branch, String targetDirectory) {
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
            // Works for both Windows and Linux
            f.createNewFile();

            PrintWriter fileOut = new PrintWriter(f);
            fileOut.println("JBOSS-OUTPUT-DIR=" + outputDirectory);
            fileOut.println("client-name=" + project);
            fileOut.close();
        } catch (IOException ex) {
            log.error(ex);
        }

        runCMDCommand("cd " + projectLocation + "\\app-builder & mvn -Dclient-name=" + project + " clean package");
    }

    // Figure out the absolute path for if the application is being run via jar or compiled within an IDE
    public static String getResourcePath() {
        String resourcesPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            if (resourcesPath.contains("SDE3.jar")) {
                resourcesPath = resourcesPath.replace("SDE3.jar", ""); // Removes the jar name
                resourcesPath = resourcesPath.substring(1); // Removes an initial / from the start of the string
                resourcesPath = URLDecoder.decode(resourcesPath + "../resources", "UTF-8");
            } else {
                resourcesPath = resourcesPath.substring(1); // Removes an initial / from the start of the string
                resourcesPath = URLDecoder.decode(resourcesPath, "UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        }

        return resourcesPath;
    }

    // Figure out the absolute path for if the application is being run via jar or compiled within an IDE
    public static String getNodeImplementationsClassPath() {
        String resourcesPath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            if (resourcesPath.contains("SDE3.jar")) {
                resourcesPath = resourcesPath.substring(1); // Removes an initial / from the start of the string
                resourcesPath = URLDecoder.decode(resourcesPath + "!/application", "UTF-8");
            } else {
                resourcesPath = resourcesPath.substring(1); // Removes an initial / from the start of the string
                resourcesPath = URLDecoder.decode(resourcesPath, "UTF-8");
            }
        } catch (UnsupportedEncodingException ex) {
            log.error(ex);
        }

        return resourcesPath;
    }
}
