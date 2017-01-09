package application.test.core;

import application.error.Error;
import application.gui.UI;
import application.node.objects.Test;
import application.test.TestCommand;
import application.test.structure.BaseStructure;
import application.test.structure.Click;
import application.test.structure.URL;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.apache.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TestStructure {
    private static HashMap<String, Class> structureClasses = new HashMap<>();
    private static Logger log = Logger.getLogger(TestStructure.class);

    /*
        We need to manually link which written commands link to which classes.
     */
    static {
        structureClasses.put("click", Click.class);
        structureClasses.put("url", URL.class);
    }

    private List<BaseStructure> baseStructures = new ArrayList<>();


    public TestStructure() {

    }

    private TestStructure(String testText) {
        baseStructures.clear();
        List<String> commands = new ArrayList<>();
        Collections.addAll(commands, testText.split("[\\r\\n]"));

        // Go through each command and create the required object
        for (String command : commands) {
            TestCommand testCommand = TestCommand.parseCommand(command);
            testCommand.setCommandLineNumber(baseStructures.size() + 1); // Sets the command position via line number

            if (command.startsWith("//") || command.equals("") || testCommand == null) { // Ignore the command if it is a comment
                continue;
            }
            if (structureClasses.containsKey(testCommand.getMainCommand())) {
                Class commandClass = structureClasses.get(testCommand.getMainCommand());
                try {
                    BaseStructure baseStructure = (BaseStructure) commandClass.getConstructor(TestCommand.class).newInstance(testCommand);
                    baseStructures.add(baseStructure);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                    Error.UNABLE_TO_FIND_CLASS_CONSTRUCTOR.record().create(ex);
                }
            } else {
                log.info("Command " + command + " does not exist");
            }
        }
    }

    public static TestStructure create(Test test) {
        if (test == null) return null;
        return create(test.getText());
    }

    public static TestStructure create(String textText) {
        return new TestStructure(textText);
    }

    public Node getInterface() {
        VBox vBox = new VBox(5);
        UI.setAnchorMargins(vBox, 10.0, 10.0, 10.0, 10.0);

        for (BaseStructure baseStructure : baseStructures) {
            vBox.getChildren().add(baseStructure.createSupportingInterface());
        }

        return vBox;
    }

    public String toScript() {
        StringBuilder sb = new StringBuilder();
        for (BaseStructure baseStructure : baseStructures) {
            sb.append(baseStructure.toScript()).append("\n");
        }
        return sb.toString();
    }
}
