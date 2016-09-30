package application.utils;

import application.Main;
import application.error.Error;
import application.gui.FlowController;
import application.net.ssh.SSHCommand;
import application.net.ssh.SSHManager;
import application.node.design.DrawableNode;
import application.node.implementations.ConsoleNode;
import application.node.implementations.LinuxNode;
import com.jcraft.jsch.JSch;
import org.apache.commons.collections.IteratorUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.zeroturnaround.zip.ZipUtil;
import us.codecraft.xsoup.Xsoup;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

public class SDEUtils {
    private static Logger log = Logger.getLogger(SDEUtils.class);
    private static String knownHosts;

    static {
        try {
            knownHosts = new File(System.getProperty("user.home"), ".ssh/known_hosts").getCanonicalPath();
        } catch (IOException ex) {
            Error.KNOWN_HOSTS.record().create(ex);
        }
    }

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
            Error.RUN_CMD_COMMAND.record().create(ex);
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
            Error.FOCAL_BUILD_APPLICATION.record().create(ex);
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
            Error.RESOURCE_PATH.record().create(ex);
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
            Error.NODE_IMPLEMENTATION.record().create(ex);
        }

        return resourcesPath;
    }

    public static Element getJSoupElementFromWebElement(WebElement webElement, Document document) {
        return getElementFromXPath(generateXPath(webElement), document);
    }

    public static String generateXPath(WebElement webElement) {
        return generateXPath(webElement, true);
    }

    public static String generateXPath(WebElement webElement, Boolean withId) {
        if (webElement == null) return null;
        if (!webElement.getAttribute("id").equals("") && withId) { // If the element has an ID we just use that as we found an anchor
            return "//*[@id=\"" + webElement.getAttribute("id") + "\"]";
        } else { // If not we put the tag and go up
            if ("body".equals(webElement.getTagName())) {
                return "/html/body";
            } else {
                WebElement parent = webElement.findElement(By.xpath(".."));
                List<WebElement> parentTagList = parent.findElements(By.tagName(webElement.getTagName()));

                if (parentTagList.size() == 1) { // If the tag was the only one of its kind we don't need to specify index
                    return generateXPath(parent, withId) + "/" + webElement.getTagName();
                } else { // We need to specify index here
                    return generateXPath(parent, withId) + "/" + webElement.getTagName() + "[" + (parentTagList.indexOf(webElement) + 1) + "]";
                }
            }
        }
    }

    public static String generateXPath(Element element) {
        return generateXPath(element, true);
    }

    public static String generateXPath(Element element, Boolean withId) {
        if (!element.attr("id").equals("") && withId) { // If the element has an ID we just use that as we found an anchor
            return "//*[@id=\"" + element.attr("id") + "\"]";
        } else { // If not we put the tag and go up
            Element parent = element.parent();
            // If there is no parent there is nothing to add, we are at the top of the tree
            if (parent == null) {
                return "";
            }
            // We only only direct children for xPath
            List<Element> parentTagList = new ArrayList<>();
            for (Element childElement : parent.children()) {
                if (childElement.tagName().equals(element.tagName())) { // Only count the children with the same tags as our element
                    parentTagList.add(childElement);
                }
            }

            if (parentTagList.size() == 1) { // If the tag was the only one of its kind we don't need to specify index
                return generateXPath(parent, withId) + "/" + element.tagName();
            } else { // We need to specify index here
                return generateXPath(parent, withId) + "/" + element.tagName() + "[" + (parentTagList.indexOf(element) + 1) + "]";
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

    public static Element getElementFromXPath(String xPath, Document document) {
        List<Element> elements = getElementsFromXPath(xPath, document);
        if (elements != null && elements.size() > 0) {
            return elements.get(0);
        } else {
            return null;
        }
    }

    public static List<Element> getElementsFromXPath(String xPath, Document document) {
        if (xPath == null) return null;
        return (List<Element>) IteratorUtils.toList(Xsoup.compile(xPath).evaluate(document).getElements().listIterator());
    }

    public static void zipDirectory(String directoryPath, String zipFileLocation) {
        ZipUtil.pack(new File(directoryPath), new File(zipFileLocation));
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static String removeNonNumericCharacters(String str) {
        return str.replaceAll("[^\\d+\\.+-]", "");
    }

    public static Double parseDouble(String doubleValue) {
        try {
            return Double.parseDouble(doubleValue);
        } catch (NumberFormatException ex) {
            Error.PARSE_DOUBLE_FAILED.record().additionalInformation(doubleValue).hideStackInLog().create(ex);
        }

        return 0d;
    }
}
