package application.test.action;

import application.test.TestParameter;
import application.test.action.helpers.Loop;
import application.test.action.helpers.LoopedObject;
import application.test.action.helpers.LoopedWebElement;
import application.test.action.helpers.LoopedWindowHandle;
import application.utils.SDEUtils;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This action starts and stops a loop section.
 * <p>
 * A loop will take a list and iterate over it allowing each element to be accessed via the loop variable to be used
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
        TestParameter startElement = getTestCommand().getParameterByName("start");
        TestParameter endElement = getTestCommand().getParameterByName("end");

        if (startElement.exists()) {
            String loopRef = startElement.getParameterValue();

            if (getLoopTracker().getLoop(loopRef) == null) {
                Loop newLoop = new Loop(getRunningTest().getCurrentLine());

                TestParameter loopRepeatCount = getTestCommand().getParameterByName("repeat");
                TestParameter loopList = getTestCommand().getParameterByName("list");
                if (loopRepeatCount.exists()) {
                    newLoop.setLoopUntil(Integer.parseInt(loopRepeatCount.getParameterValue()));
                    newLoop.setCurrentLoopCount(1);
                    newLoop.setLoopType("LoopCount");
                } else if (loopList.exists()) {
                    TestParameter listTag = getTestCommand().getParameterByPath("list::tag");
                    TestParameter listWindow = getTestCommand().getParameterByPath("list::window");
                    TestParameter listRootElementId = getTestCommand().getParameterByPath("id");
                    TestParameter listRootElementXPath = getTestCommand().getParameterByPath("xPath");
                    TestParameter filteredHasClass = getTestCommand().getParameterByPath("filter::hasClass");
                    TestParameter filteredSelect = getTestCommand().getParameterByPath("filter::select");

                    TestParameter loopElement = getTestCommand().getParameterByName("loop");
                    TestParameter directChildren = getTestCommand().getParameterByName("direct");

                    List<Element> elements = new ArrayList<>();
                    List<LoopedObject> loopedObjects;
                    if (listTag.exists()) {
                        if (listRootElementId.exists() || listRootElementXPath.exists() || loopElement.exists()) {
                            String xPath = null;

                            if (listRootElementXPath.exists()) {
                                xPath = listRootElementXPath.getParameterValue();
                            } else if (listRootElementId.exists()) {
                                xPath = "//*[@id=\"" + listRootElementId.getParameterValue() + "\"]";
                            }

                            Element listElement = null;
                            if (loopElement.exists()) {
                                Loop loop = getLoopTracker().getLoop(loopElement.getParameterValue());
                                if (loop != null) {
                                    LoopedWebElement loopedWebElement = (LoopedWebElement) loop.getCurrentLoopObject();
                                    if (loopedWebElement != null) {
                                        listElement = loopedWebElement.getElement();
                                    }
                                }
                                //log.info("Found elements via loop of " + listElement);
                            } else {
                                listElement = SDEUtils.getElementFromXPath(xPath, getDocumentTracker().getCurrentDocument());
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
                            }
                        } else {
                            elements = getDocumentTracker().getCurrentDocument().getElementsByTag(listTag.getParameterValue());
                        }
                    }

                    // Apply any filters to the loop
                    if (filteredHasClass.exists()) {
                        List<Element> elementsToRemove = new ArrayList<>();
                        for (Element element : elements) { // Remove the element if it does not have this class
                            if (!element.hasClass(filteredHasClass.getParameterValue())) {
                                elementsToRemove.add(element);

                            }
                        }
                        elements.removeAll(elementsToRemove);


                    }

                    // Add the looped elements to a handling wrapper
                    loopedObjects = elements.stream().map(LoopedWebElement::new).collect(Collectors.toList());

                    // Get all current windows handles, to loop through them
                    if (listWindow.exists()) {
                        for (String handle : getDriver().getWindowHandles()) {
                            loopedObjects.add(new LoopedWindowHandle(handle));
                        }
                    }

                    newLoop.setLoopElements(loopedObjects);
                    newLoop.setLoopUntil(loopedObjects.size());
                    if (loopedObjects.size() == 0) {
                        newLoop.setCurrentLoopCount(0);
                    } else {
                        newLoop.setCurrentLoopCount(1);
                    }

                    newLoop.setLoopType("LoopList");
                }

                getLoopTracker().setLoop(loopRef, newLoop);
            }
        }

        if (endElement.exists()) {
            String loopRef = endElement.getParameterValue();

            if (getLoopTracker().getLoop(loopRef) != null) {
                Loop loop = getLoopTracker().getLoop(loopRef);

                if ("LoopCount".equals(loop.getLoopType())) {
                    if (!loop.getLoopUntil().equals(loop.getCurrentLoopCount())) {
                        getRunningTest().setCurrentLine(loop.getStartLineNumber());
                        loop.setCurrentLoopCount(loop.getCurrentLoopCount() + 1);
                    } else {
                        getLoopTracker().removeLoop(loopRef);
                    }
                } else if ("LoopList".equals(loop.getLoopType())) {
                    if (!loop.getLoopUntil().equals(loop.getCurrentLoopCount())) {
                        getRunningTest().setCurrentLine(loop.getStartLineNumber());
                        loop.setCurrentLoopCount(loop.getCurrentLoopCount() + 1);
                    } else {
                        getLoopTracker().removeLoop(loopRef);
                    }
                }
            }
        }
    }
}
