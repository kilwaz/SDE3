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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.zeroturnaround.zip.ZipUtil;

import java.io.*;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

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
                boolean fileDelete = f.delete();
            }
            // Works for both Windows and Linux
            boolean result = f.createNewFile();

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

    public static String generateXPath(WebElement webElement) {
        if (!webElement.getAttribute("id").equals("")) { // If the element has an ID we just use that as we found an anchor
            return "//*[@id=\"" + webElement.getAttribute("id") + "\"]";
        } else { // If not we put the tag and go up
            WebElement parent = webElement.findElement(By.xpath(".."));
            List<WebElement> parentTagList = parent.findElements(By.tagName(webElement.getTagName()));

            if (parentTagList.size() == 1) { // If the tag was the only one of its kind we don't need to specify index
                return generateXPath(parent) + "/" + webElement.getTagName();
            } else { // We need to specify index here
                return generateXPath(parent) + "/" + webElement.getTagName() + "[" + (parentTagList.indexOf(webElement) + 1) + "]";
            }
        }
    }

    public static String generateXPath(Element element) {
        if (!element.attr("id").equals("")) { // If the element has an ID we just use that as we found an anchor
            return "//*[@id=\"" + element.attr("id") + "\"]";
        } else { // If not we put the tag and go up
            Element parent = element.parent();
            // We only only direct children for xPath
            List<Element> parentTagList = new ArrayList<>();
            for (Element childElement : parent.children()) {
                if (childElement.tagName().equals(element.tagName())) { // Only count the children with the same tags as our element
                    parentTagList.add(childElement);
                }
            }

            if (parentTagList.size() == 1) { // If the tag was the only one of its kind we don't need to specify index
                return generateXPath(parent) + "/" + element.tagName();
            } else { // We need to specify index here
                return generateXPath(parent) + "/" + element.tagName() + "[" + (parentTagList.indexOf(element) + 1) + "]";
            }
        }
    }

    public static String unescapeXMLCData(String source) {
        source = source.replace("&amp;", "&");
        source = source.replace("&gt;", ">");
        return source;
    }

    public static String escapeXMLCData(String source) {
        source = source.replace("&", "&amp;");
        source = source.replace(">", "&gt;");
        return source;
    }

//    public static Elements getElementsFromXPath(String xPath, Element element) {
//
//    }

    public static Element getElementFromXPath(String xPath, Document document) {
        List<Element> elements = getElementsFromXPath(xPath, document);
        if (elements.size() > 0) {
            return elements.get(0);
        } else {
            return null;
        }
    }

    public static List<Element> getElementsFromXPath(String xPath, Document document) {
        String startingId = xPath;
        startingId = startingId.replace("//*[@id=\"", "");  // Removes the initial id
        startingId = startingId.substring(0, startingId.indexOf("\""));

        //log.info("Document is " + document);

        Element startingElement = document.getElementById(startingId);

        List<Element> returnedElement = new ArrayList<>();

        if (startingElement != null) {
            returnedElement.add(startingElement);
        }

        return returnedElement;
    }

    public static void zipDirectory(String directoryPath, String zipFileLocation) {
        ZipUtil.pack(new File(directoryPath), new File(zipFileLocation));
    }
}
