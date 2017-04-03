package application.testing;

import application.Main;
import application.data.DBConnectionManager;
import application.gui.Program;
import application.node.design.DrawableNode;
import com.jayway.awaitility.Awaitility;
import javafx.application.Application;
import javafx.application.Platform;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * This class is designed to provide common functions that will be used in JUnit tests.
 */
public class TestingUtils {
    private static Thread javaFXThread;

    /**
     * This can be used as a check before running any application methods that might use the GUI or database before they have been initialised.
     *
     * @return Returns {@link java.lang.Boolean} whether the application has been initialised and things like database and logging are available to use.
     */
    private static Callable<Boolean> mainIsReady() {
        return () -> {
            return Main.getInstance() != null; // The condition that must be fulfilled
        };
    }

    /**
     * This will setup JavaFX correctly and launch the application so that it is in a usable state for testing.
     */
    public synchronized static void setupJavaFX() {
        if (javaFXThread == null) {
            javaFXThread = new Thread("JavaFX Init Thread") {
                @Override
                public void run() {
                    Application.launch(Main.class);
                }
            };
            javaFXThread.setDaemon(true);
            javaFXThread.start();
        }

        Awaitility.await().atMost(15, TimeUnit.SECONDS).until(mainIsReady());
    }

    /**
     * @return Returns the created {@link application.gui.Program}.
     */
    public static Program createTestProgram() {
        Program testProgram = Program.create(Program.class);
        testProgram.setName("TestProgram");
        return testProgram;
    }

    /**
     * This class can be used to create test nodes that we want to perform specific tests against.
     * These test nodes will be saved in the database so and so must be cleaned up after the tests are completed by deleting them.
     *
     * @param testProgram The {@link application.gui.Program} that this node should be added to.  Typically a test only program.
     * @param testClass   A {@link java.lang.Class} reference to the node implementation we want to create.
     * @return Returns the created {@link application.node.design.DrawableNode}.
     */
    public static DrawableNode createTestNode(Program testProgram, Class<DrawableNode> testClass) {
        DrawableNode drawableNode = DrawableNode.create(testClass);
        drawableNode.setProgram(testProgram);
        return drawableNode;
    }

    /**
     * Creates the application database connection and assigns the user.
     */
    public static void initDatabase() {
        DBConnectionManager.getInstance().createApplicationConnection();
    }

    /**
     * This can be called at the end of a test during the after class section in order to close down the current test application database connection and clean up the test properly.
     */
    public static void closeDatabase() {
        DBConnectionManager.getInstance().closeConnections();
    }

    /**
     * This will close the JavaFX platform down after testing has finished.
     */
    public static void shutdownJavaFx() {
        Platform.exit();
    }
}
