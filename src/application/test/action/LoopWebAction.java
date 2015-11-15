package application.test.action;

import application.data.DataBank;
import application.test.TestParameter;
import application.test.TestStep;
import application.test.action.helpers.Loop;
import application.test.action.helpers.LoopTracker;
import application.test.action.helpers.LoopedWebElement;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This action starts and stops a loop section.
 *
 * A loop will take a list and interate over it allowing each element to be accessed via the loop variable to be used
 * for other actions.
 */
public class LoopWebAction extends WebAction {
    private static Logger log = Logger.getLogger(LoopWebAction.class);

    public LoopWebAction() {
    }

    /**
     * Run by {@link WebAction} to handle this action.
     */
    public void performAction() {
        TestStep testStep = DataBank.createNewTestStep(getTestResult());
        getTestResult().addTestStep(testStep);

        TestParameter startElement = getTestCommand().getParameterByName("start");
        TestParameter endElement = getTestCommand().getParameterByName("end");

        if (startElement.exists()) {
            String loopRef = startElement.getParameterValue();

            if (LoopTracker.getLoop(loopRef) == null) {
                Loop newLoop = new Loop(getParentTestNode().getCurrentTestLine());

                TestParameter loopRepeatCount = getTestCommand().getParameterByName("repeat");
                TestParameter loopList = getTestCommand().getParameterByName("list");
                if (loopRepeatCount.exists()) {
                    newLoop.setLoopUntil(Integer.parseInt(loopRepeatCount.getParameterValue()));
                    newLoop.setCurrentLoopCount(1);
                    newLoop.setLoopType("LoopCount");
                } else if (loopList.exists()) {
                    TestParameter listTag = getTestCommand().getParameterByPath("list::tag");
                    TestParameter listRootElementId = getTestCommand().getParameterByPath("id");
                    TestParameter listRootElementXPath = getTestCommand().getParameterByPath("xPath");
                    TestParameter loopElement = getTestCommand().getParameterByName("loop");
                    TestParameter directChildren = getTestCommand().getParameterByName("direct");

                    List<Element> elements = new ArrayList<>();
                    if (listRootElementId.exists() || listRootElementXPath.exists() || loopElement.exists()) {
                        String xPath = null;

                        if (listRootElementXPath.exists()) {
                            xPath = listRootElementXPath.getParameterValue();
                        } else if (listRootElementId.exists()) {
                            xPath = "//*[@id=\"" + listRootElementId.getParameterValue() + "\"]";
                        }

                        Element listElement = null;
                        if (loopElement.exists()) {
                            Loop loop = LoopTracker.getLoop(loopElement.getParameterValue());
                            if (loop != null) {
                                LoopedWebElement loopedWebElement = loop.getCurrentLoopWebElement();
                                if (loopedWebElement != null) {
                                    listElement = loopedWebElement.getElement();
                                }
                            }
                            //log.info("Found elements via loop of " + listElement);
                        } else {
                            listElement = SDEUtils.getElementFromXPath(xPath, getCurrentDocument());
                        }

                        if (listElement != null) {
                            if (directChildren.exists()) {
                                // We only want direct children from this
                                for (Element childElement : listElement.children()) {
                                    if (childElement.tagName().equals(listTag.getParameterValue())) {
                                        elements.add(childElement);
                                    }
                                }
                            } else {
                                elements = listElement.getElementsByTag(listTag.getParameterValue());
                            }

                            //elements = listElement.getElementsByTag(listTag.getParameterValue());
                            //log.info("Found elements inside " + elements);
                        }
                    } else {
                        elements = getCurrentDocument().getElementsByTag(listTag.getParameterValue());
                    }

                    // Add the looped elements to a handling wrapper
                    List<LoopedWebElement> loopedElements = elements.stream().map(LoopedWebElement::new).collect(Collectors.toList());

                    newLoop.setLoopElements(loopedElements);
                    newLoop.setLoopUntil(elements.size());
                    if (elements.size() == 0) {
                        newLoop.setCurrentLoopCount(0);
                    } else {
                        newLoop.setCurrentLoopCount(1);
                    }

                    newLoop.setLoopType("LoopList");
                }

                LoopTracker.setLoop(loopRef, newLoop);
            }
        }

        if (endElement.exists()) {
            String loopRef = endElement.getParameterValue();

            if (LoopTracker.getLoop(loopRef) != null) {
                Loop loop = LoopTracker.getLoop(loopRef);

                if ("LoopCount".equals(loop.getLoopType())) {
                    if (!loop.getLoopUntil().equals(loop.getCurrentLoopCount())) {
                        getParentTestNode().setCurrentTestLine(loop.getStartLineNumber());
                        loop.setCurrentLoopCount(loop.getCurrentLoopCount() + 1);
                    } else {
                        LoopTracker.removeLoop(loopRef);
                    }
                } else if ("LoopList".equals(loop.getLoopType())) {
                    if (!loop.getLoopUntil().equals(loop.getCurrentLoopCount())) {
                        getParentTestNode().setCurrentTestLine(loop.getStartLineNumber());
                        loop.setCurrentLoopCount(loop.getCurrentLoopCount() + 1);
                    } else {
                        LoopTracker.removeLoop(loopRef);
                    }
                }
            }
        }

        testStep.save();
    }
}
