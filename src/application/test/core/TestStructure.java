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
    private Test test;


    public TestStructure() {

    }

    private TestStructure(Test test) {
        this.test = test;
        baseStructures.clear();
        List<String> commands = new ArrayList<>();
        Collections.addAll(commands, test.getText().split("[\\r\\n]"));

        // Go through each command and create the required object
        for (String command : commands) {
            TestCommand testCommand = TestCommand.parseCommand(command);
            if (testCommand != null) {
                testCommand.setCommandLineNumber(baseStructures.size() + 1); // Sets the command position via line number

                if (command.startsWith("//") || command.equals("") || testCommand == null) { // Ignore the command if it is a comment
                    continue;
                }
                if (structureClasses.containsKey(testCommand.getMainCommand())) {
                    Class commandClass = structureClasses.get(testCommand.getMainCommand());
                    try {
                        BaseStructure baseStructure = (BaseStructure) commandClass.getConstructor(TestCommand.class, TestStructure.class).newInstance(testCommand, this);
                        baseStructures.add(baseStructure);
                    } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                        Error.UNABLE_TO_FIND_CLASS_CONSTRUCTOR.record().create(ex);
                    }
                } else {
                    log.info("Command " + command + " does not exist");
                }
            }
        }
    }

    public static TestStructure create(Test test) {
        if (test == null) return null;
        return new TestStructure(test);
    }

    public void swapBaseStructures(Integer position1, Integer position2) {
        getBaseStructureAtPosition(position1 - 1).setLineNumber(position2);
        getBaseStructureAtPosition(position2 - 1).setLineNumber(position1);

        Collections.swap(baseStructures, position1 - 1, position2 - 1);

        test.setText(toScript());
    }

    public BaseStructure getBaseStructureAtPosition(Integer position) {
        return baseStructures.get(position);
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
