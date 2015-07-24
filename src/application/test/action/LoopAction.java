package application.test.action;

import application.data.DataBank;
import application.test.*;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

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
            String loopRef = startElement.getParameterValue();

            if (LoopTracker.getLoop(loopRef) == null) {
                Loop newLoop = new Loop(getParentTestNode().getCurrentTestLine());

                TestParameter loopRepeatCount = getTestCommand().getParameterByName("repeat");
                TestParameter loopList = getTestCommand().getParameterByName("list");
                if (loopRepeatCount != null) {
                    newLoop.setLoopUntil(Integer.parseInt(loopRepeatCount.getParameterValue()));
                    newLoop.setCurrentLoopCount(1);
                    newLoop.setLoopType("LoopCount");
                } else if (loopList != null) {
                    TestParameter listTag = getTestCommand().getParameterByPath("list::tag");
                    TestParameter listRootElementId = getTestCommand().getParameterByPath("id");
                    TestParameter listRootElementXPath = getTestCommand().getParameterByPath("xPath");
                    TestParameter loopElement = getTestCommand().getParameterByName("loop");
                    TestParameter directChildren = getTestCommand().getParameterByName("direct");

                    List<Element> elements = new ArrayList<>();
                    if (listRootElementId != null || listRootElementXPath != null || loopElement != null) {
                        String xPath = null;

                        if (listRootElementXPath != null) {
                            xPath = listRootElementXPath.getParameterValue();
                        } else if (listRootElementId != null) {
                            xPath = "//*[@id=\"" + listRootElementId.getParameterValue() + "\"]";
                        }

                        Element listElement;
                        if (loopElement != null) {
                            listElement = LoopTracker.getLoop(loopElement.getParameterValue()).getCurrentLoopWebElement().getElement();
                            //log.info("Found elements via loop of " + listElement);
                        } else {
                            listElement = SDEUtils.getElementFromXPath(xPath, getCurrentDocument());
                        }

                        if (listElement != null) {
                            if (directChildren != null) {
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

        if (endElement != null) {
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

        DataBank.saveTestStep(testStep);
    }
}
