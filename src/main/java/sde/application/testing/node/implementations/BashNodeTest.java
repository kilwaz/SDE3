package sde.application.testing.node.implementations;

import sde.application.gui.Program;
import sde.application.node.implementations.BashNode;
import sde.application.testing.JavaFXThreadingRule;
import sde.application.testing.TestingUtils;
import javafx.scene.paint.Color;
import org.junit.*;
import sde.application.node.design.DrawableNode;
import sde.application.node.objects.Bash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JUnit test class for {@link BashNode}.
 *
 * @author Alex Brown
 */

public class BashNodeTest {
    private Double bashX = 1d;
    private Double bashY = 2d;
    private Double bashWidth = 10d;
    private Double bashHeight = 11d;
    private Double bashScale = 1.5;

    private String bashContainedText = "ThisBashNode";
    private String bashNextNodeToRun = "NextNode";
    private String bashBash = "BashBash";

    private Color bashColor = Color.RED;

    // Static test variables made for this test
    private static BashNode testBashNode;
    private static Program testProgram;

    @Rule
    public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();

    /**
     * Prep database with new test program
     *
     * @throws InterruptedException
     */
    @BeforeClass
    public static void setMyClass() throws InterruptedException {
        TestingUtils.setupJavaFX();
        TestingUtils.initDatabase();
        testProgram = TestingUtils.createTestProgram();
    }

    /**
     * Create new node against test program and set initial values
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        testBashNode = BashNode.create(BashNode.class);
        testBashNode.setX(bashX);
        testBashNode.setY(bashY);
        testBashNode.setWidth(bashWidth);
        testBashNode.setHeight(bashHeight);
        testBashNode.setColor(bashColor);
        testBashNode.setScale(bashScale);
        testBashNode.setContainedText(bashContainedText);
        testBashNode.setNextNodeToRun(bashNextNodeToRun);
        testBashNode.setBash(bashBash);
    }

    /**
     * Clean up test node
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        testBashNode.delete();
    }

    /**
     * Cleanup test program and close database
     */
    @AfterClass
    public static void afterClass() {
        testProgram.delete();
        TestingUtils.closeDatabase();
    }

    /**
     * Test bash after <code>null</code> insert
     * The expected result would be receive a {@link Bash} Object but with an empty string script content.
     *
     * @throws Exception
     */
    @Test
    public void testNullBash() throws Exception {
        testBashNode.setBash(null);
        assertNotNull(testBashNode.getBash());
        assertEquals("", testBashNode.getBash().getScript());
    }

    /**
     * Test Bash object returns correctly after giving new script
     * The expected result would be for the returned {@link Bash} Object to have the same script as what we passed in
     *
     * @throws Exception
     */
    @Test
    public void testBashScriptContent() throws Exception {
        testBashNode.setBash("bashTestScript");
        assertNotNull(testBashNode.getBash());
        assertEquals("bashTestScript", testBashNode.getBash().getScript());
    }

    /**
     * Copy the BashNode
     * Here we are testing copying a {@link BashNode} node, it should duplicate all values that can be passed to it
     * The expected result would be for all values to be equal after the copy
     *
     * @throws Exception
     */
    @Test
    public void testCopyNode() throws Exception {
        BashNode bashToCopyTo = new BashNode(testBashNode);
        checkAllValuesForNodeAreSame(bashToCopyTo, testBashNode);
    }

    /**
     * Tests the generic copy method to see if it is working correctly for this node.
     * <p>
     * The copy method is held in {@link DrawableNode}.
     *
     * @throws Exception
     */
    @Test
    public void testCopyNodeGenericMethod() throws Exception {
        BashNode bashToCopyTo = testBashNode.copy();
        checkAllValuesForNodeAreSame(bashToCopyTo, testBashNode);
    }

    /**
     * Compares two nodes to check they are the same by going through each value and asserting it
     *
     * @param node1 First {@link BashNode} to compare.
     * @param node2 Second {@link BashNode} to compare.
     */
    public void checkAllValuesForNodeAreSame(BashNode node1, BashNode node2) {
        assertEquals(node1.getX(), node2.getX());
        assertEquals(node1.getY(), node2.getY());
        assertEquals(node1.getProgramUuid(), node2.getProgramUuid());
        assertEquals(node1.getWidth(), node2.getWidth());
        assertEquals(node1.getHeight(), node2.getHeight());
        assertEquals(node1.getScale(), node2.getScale());
        assertEquals(node1.getContainedText(), node2.getContainedText());
        assertEquals(node1.getNextNodeToRun(), node2.getNextNodeToRun());
        assertEquals(node1.getColor(), node2.getColor());
        assertEquals(node1.getBash().getScript(), node2.getBash().getScript());
    }
}
