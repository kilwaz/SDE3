package application.test.action;

import application.data.DataBank;
import application.test.*;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LoopAction extends ActionControl {
    private static Logger log = Logger.getLogger(LoopAction.class);

    // This class is used to input a value into an element
    public LoopAction() {
    }

    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter startElement = getTestCommand().getParameterByName("start");
        TestParameter endElement = getTestCommand().getParameterByName("end");

        if (startElement != null) {
            log.info("Loop start " + startElement.getParameterValue());

            String loopRef = startElement.getParameterValue();

            log.info("Loop reference is " + loopRef);

            if (LoopTracker.getLoop(loopRef) == null) {
                Loop newLoop = new Loop(getParentTestNode().getCurrentTestLine());
                log.info("Saved the start number as " + newLoop.getStartLineNumber());

                TestParameter loopRepeatCount = getTestCommand().getParameterByName("repeat");
                TestParameter loopList = getTestCommand().getParameterByName("list");
                if (loopRepeatCount != null) {
                    newLoop.setLoopUntil(Integer.parseInt(loopRepeatCount.getParameterValue()));
                    newLoop.setCurrentLoopCount(1);
                    newLoop.setLoopType("LoopCount");
                } else if (loopList != null) {

                    log.info("Starting loop list");

                    TestParameter listTag = getTestCommand().getParameterByPath("list::tag");
                    TestParameter listRootElementId = getTestCommand().getParameterByPath("element::id");
                    TestParameter listRootElementXPath = getTestCommand().getParameterByPath("element::xPath");

                    if (listTag != null) {
                        log.info("Finding elements " + listTag.getParameterValue());

                        List<WebElement> elements;
                        if (listRootElementId != null || listRootElementXPath != null) {
                            By testBy = null;

                            if (listRootElementXPath != null) {
                                testBy = findElement(listRootElementXPath);
                            } else if (listRootElementId != null) {
                                testBy = findElement(listRootElementId);
                            }
                            WebElement listElement = getDriver().findElement(testBy);
                            elements = listElement.findElements(By.tagName(listTag.getParameterValue()));
                        } else {
                            log.info("search all elements on page " + listTag.getParameterValue());
                            elements = getDriver().findElements(By.tagName(listTag.getParameterValue()));
                        }

                        log.info("Looping through a list of " + elements.size() + " elements");

                        // Add the looped elements to a handling wrapper
                        List<LoopedWebElement> loopedElements = elements.stream().map(LoopedWebElement::new).collect(Collectors.toList());

                        newLoop.setLoopElements(loopedElements);
                        newLoop.setLoopUntil(elements.size());
                        newLoop.setCurrentLoopCount(1);
                    }

                    newLoop.setLoopType("LoopList");
                }

                LoopTracker.setLoop(loopRef, newLoop);
            }
        }

        if (endElement != null) {
            log.info("Loop end " + endElement.getParameterValue());

            String loopRef = endElement.getParameterValue();

            log.info("Loop reference is " + loopRef);

            if (LoopTracker.getLoop(loopRef) != null) {
                Loop loop = LoopTracker.getLoop(loopRef);

                if ("LoopCount".equals(loop.getLoopType())) {
                    if (!loop.getLoopUntil().equals(loop.getCurrentLoopCount())) {
                        getParentTestNode().setCurrentTestLine(loop.getStartLineNumber());
                        loop.setCurrentLoopCount(loop.getCurrentLoopCount() + 1);
                        log.info("Sending the loop back to start at " + loop.getStartLineNumber());
                    } else {
                        log.info("Exiting out the loop now");
                        LoopTracker.removeLoop(loopRef);
                    }
                } else if ("LoopList".equals(loop.getLoopType())) {
                    log.info("Looping in a list");
                    if (!loop.getLoopUntil().equals(loop.getCurrentLoopCount())) {
                        getParentTestNode().setCurrentTestLine(loop.getStartLineNumber());
                        loop.setCurrentLoopCount(loop.getCurrentLoopCount() + 1);
                    } else {
                        log.info("Exiting out the loop now");
                        LoopTracker.removeLoop(loopRef);
                    }
                }
            }
        }

        DataBank.saveTestStep(testStep);
    }
}
