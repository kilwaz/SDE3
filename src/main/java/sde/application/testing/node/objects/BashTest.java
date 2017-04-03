package sde.application.testing.node.objects;

import sde.application.gui.Program;
import sde.application.node.implementations.BashNode;
import sde.application.node.objects.Bash;
import sde.application.testing.JavaFXThreadingRule;
import sde.application.testing.TestingUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JUnit test class for {@link Bash}.
 *
 * @author Alex Brown
 */
public class BashTest {
    // Static test variables made for this test
    private static Bash testBash;
    private static BashNode testBashNode;
    private static Program testProgram;

    @Rule
    public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @BeforeClass
    /**
     * Prep database with new test program
     */
    public static void setMyClass() throws InterruptedException {
        TestingUtils.setupJavaFX();
        TestingUtils.initDatabase();
        testProgram = TestingUtils.createTestProgram();
        //testBashNode = (BashNode) TestingUtils.createTestNode(testProgram, BashNode.class);
    }

    @Before
    public void setUp() throws Exception {
        testBash = new Bash(testBashNode);
    }

    @AfterClass
    /**
     * Cleanup test {@link Program} and test {@link BashNode} and close database connections.
     */
    public static void afterClass() {
        testBashNode.delete();
        testProgram.delete();
        TestingUtils.closeDatabase();
    }

    @Test
    public void testNullParentBashNode() throws Exception {
        exception.expect(NullPointerException.class);
        new Bash(null);
    }

    /**
     * @throws Exception
     */
    @Test
    public void testGetParentBashNode() throws Exception {
        assertEquals(testBash.getParentBashNode(), testBashNode);
    }

    @Test
    public void testGetScript() throws Exception {
        String defaultScript = "#!/bin/bash\n" +
                "VAR=\"hello\"\n" +
                "echo $VAR";

        assertNotNull(testBash.getScript());
        assertEquals(defaultScript, testBash.getScript());
    }

    @Test
    public void testSetScript() throws Exception {
        testBash.setScript("NewScript");
        assertEquals(testBash.getScript(), "NewScript");
    }

    @Test
    public void testGetId() throws Exception {
        assertNotNull(testBash.getId());  // This should never be null
        assertEquals(Integer.valueOf(-1), testBash.getId());  // If we are creating the object ourselves then this should actually be -1
    }

    @Test
    public void testSetId() throws Exception {
        testBash.setId(1);
        assertEquals(Integer.valueOf(1), testBash.getId());
    }
}
