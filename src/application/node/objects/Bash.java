package application.node.objects;

import application.data.DataBank;
import application.node.implementations.BashNode;
import org.apache.log4j.Logger;
import application.error.Error;

/**
 * This class handles the script used within {@link application.node.implementations.BashNode}.
 *
 * @author Alex Brown
 */
public class Bash {
    private String script;
    private BashNode parentBashNode;
    private Integer id = -1;

    private static Logger log = Logger.getLogger(Bash.class);

    private static final String defaultScript = "#!/bin/bash\n" +
            "VAR=\"hello\"\n" +
            "echo $VAR";

    /**
     * Default creation that will set the script to the default value.
     *
     * @param parentBashNode Must not be null.
     */
    public Bash(BashNode parentBashNode) {
        if (parentBashNode == null) {
            Error.BASH_NODE_PARENT_NULL.record().create();

            throw new NullPointerException();
        } else {
            this.parentBashNode = parentBashNode;
            this.script = defaultScript;
        }
    }

    /**
     * When the Object is being loaded from the database this constructor will be used to set all values
     *
     * @param parentBashNode Must not be null.
     * @param script         New script to default.
     * @param id             Database ID
     */
    public Bash(BashNode parentBashNode, String script, Integer id) {
        if (parentBashNode == null) {
            Error.BASH_NODE_PARENT_NULL.record().create();
            throw new NullPointerException();
        } else {
            this.parentBashNode = parentBashNode;
            this.script = script;
            this.id = id;
        }
    }

    /**
     * Gets the parent {@link application.node.implementations.BashNode} that owns this object
     *
     * @return Parent {@link application.node.implementations.BashNode}
     */
    public BashNode getParentBashNode() {
        return this.parentBashNode;
    }

    /**
     * Gets the script in String format
     *
     * @return Current script.
     */
    public String getScript() {
        return this.script;
    }

    /**
     * Sets the script, we only save the script if there are changes made.
     * This action saves to the database.
     *
     * @param script New script to set.
     */
    public void setScript(String script) {
        if (!this.script.equals(script)) {
            this.script = script;
            if (!parentBashNode.isInitialising()) {
                parentBashNode.save();
            }
        }
    }

    /**
     * Gets database ID of object
     *
     * @return Database ID
     */
    public Integer getId() {
        return id;
    }

    /**
     * Sets database ID of object
     *
     * @param id DataBase ID
     */
    public void setId(Integer id) {
        this.id = id;
    }
}
