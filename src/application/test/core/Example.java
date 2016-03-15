package application.test.core;

import application.test.annotation.*;
import org.apache.log4j.Logger;

import java.util.List;

// TestNodes annotation is an ordered list of the tests which need to be run, relating to the names of the nodes within the program flow.
@TestNodes(testList = {"Login", "Logout"})
@InputNodes(inputList = {"User 1"})

public class Example extends TestCase {
    private static Logger log = Logger.getLogger(Example.class);

    public Example() {
        super(Example.class);
    }

    @BuildTest
    public void buildTest() {
        for (String str : (List<String>) getTestNodeList()) {
            log.info("Test Name " + str);
        }
    }

    @ApplyInputs
    public void applyInputs() {
        for (String str : (List<String>) getTestNodeList()) {
            log.info("Test Name " + str);
        }
    }

    @RunTest
    public void runTest() {

    }

    // @BeforeCompare
    public void beforeCompare() {

    }

    // @AfterCompare
    public void afterCompare() {

    }

    // @BeforeTest
    public void beforeTest() {

    }

    // @AfterTest
    public void afterTest() {

    }

//    @Element(enabled = true)
//    public void totalProposed(ExpectedElement expectedElement) {
//        log.info("Method enabled");
//    }
}


