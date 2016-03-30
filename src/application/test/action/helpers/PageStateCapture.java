package application.test.action.helpers;

import application.error.Error;
import application.test.ChangedElement;
import application.test.ChangedElements;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PageStateCapture {
    private static final String generateHTMLjs = "return generateDOMHTML(document.getElementsByTagName(\"BODY\")[0]); " +
            "function generateDOMHTML(element) {" +
            "   var voidElements = [\"area\",\"base\",\"br\",\"col\",\"hr\",\"img\",\"input\",\"link\",\"meta\",\"param\"];" +
            "   if(element.nodeType == Node.ELEMENT_NODE){" +
            "       var html = \"<\" + element.tagName;" +
            "       for (var i = 0; i < element.attributes.length; i++) {" +
            "           var attrib = element.attributes[i];" +
            "           if (attrib.specified) {" +
            "               if(attrib.name in element){" +
            "                   html += \" \" + attrib.name + \"=\\\"\" + eval(\"element.\" + attrib.name) + \"\\\"\";" +
            "               } else {" +
            "                   html += \" \" + attrib.name + \"=\\\"\" + attrib.value + \"\\\"\";" +
            "               }" +
            "           }" +
            "       }" +
            "       var isVoidElement = (voidElements.indexOf(element.tagName.toLowerCase()) > -1);" +
            "       if(isVoidElement){" +
            "           html += \"/>\";" +
            "       } else {" +
            "       html += \">\";" +
            "       for(child in element.childNodes){" +
            "           var el = element.childNodes[child];" +
            "           html += generateDOMHTML(el);" +
            "       }" +
            "           html += \"</\" + element.tagName + \">\";" +
            "       }" +
            "       return html;" +
            "   } else if(element.nodeType == Node.TEXT_NODE) {" +
            "       return element.textContent;" +
            "   }" +
            "   return \"\";" +
            "};";
    private static Logger log = Logger.getLogger(PageStateCapture.class);
    private String elementFrame;
    private String rawSource;
    private HashMap<String, String> selectValues = new HashMap<>();
    private Document doc;
    private Elements allElements;
    private HashMap<String, Element> allElementsMap = new HashMap<>();
    private String stateName = "";

    public PageStateCapture(String elementFrame, String stateName) {
        this.elementFrame = elementFrame;
        this.stateName = stateName;
    }

    public HashMap<String, String> getSelectValues() {
        return this.selectValues;
    }

    public Elements getAllElements() {
        return this.allElements;
    }

    public HashMap<String, Element> getAllElementsMap() {
        return this.allElementsMap;
    }

    public void capturePage(WebDriver driver) {
        if (!"default".equals(elementFrame)) {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id(elementFrame)));
            driver.switchTo().frame(elementFrame);
        }

        JavascriptExecutor js = (JavascriptExecutor) driver;
        rawSource = "<HTML>" + js.executeScript(generateHTMLjs) + "</HTML>";

        doc = Jsoup.parse(rawSource);
        allElements = doc.getAllElements();

        for (Element tag : allElements) {
            if ("select".equals(tag.tagName())) {
                String javascript = "";
                try {
                    javascript = "return document.getElementById('" + tag.attr("id") + "').options[document.getElementById('" + tag.attr("id") + "').selectedIndex].text";
                    String selectResult = (String) js.executeScript(javascript);
                    selectValues.put(tag.attr("id"), selectResult);
                } catch (WebDriverException ex) {
                    Error.SELENIUM_JAVASCRIPT_FAILED.record().additionalInformation("JavaScript: " + javascript).create(ex);
                }
            }
        }

        for (Element element : allElements) {
            StringBuilder mapString = new StringBuilder();
            Element loopElement = element;
            while (loopElement != null) {
                mapString.insert(0, loopElement.tagName() + loopElement.elementSiblingIndex());
                loopElement = loopElement.parent();
            }
            allElementsMap.put(mapString.toString(), element);
        }

        if (!"default".equals(elementFrame)) {
            driver.switchTo().defaultContent();
        }
    }

    public ChangedElements compare(PageStateCapture pageStateCapture) {
        ChangedElements changedElements = new ChangedElements();

        List<Element> textElementChanges = new ArrayList<>();
        List<Element> textElementChanges2 = new ArrayList<>();
        List<Element> textElementChangesFinal = new ArrayList<>();
        List<Element> textElementChangesFinal2 = new ArrayList<>();

        HashMap<String, Element> compareAllElementsMap = pageStateCapture.getAllElementsMap();
        HashMap<String, String> compareSelectValues = pageStateCapture.getSelectValues();

        for (String reference : allElementsMap.keySet()) {
            if (compareAllElementsMap.containsKey(reference)) {
                Element tag = allElementsMap.get(reference);

                Element tag2 = compareAllElementsMap.get(reference);
                String output = "<" + tag.tagName() + " " + tag.attributes().toString() + "></" + tag.tagName() + ">";
                String output2 = "<" + tag2.tagName() + " " + tag2.attributes().toString() + "></" + tag2.tagName() + ">";
//                if ("select".equals(tag.tagName())) {
//                    String selectValue = selectValues.get(tag.attr("id"));
//                    String compareSelectValue = compareSelectValues.get(tag2.attr("id"));
//
//                    if (!selectValue.equals(compareSelectValue)) {
//                        changedElements.addElement(new ChangedElement(tag, selectValue, tag2, compareSelectValue, "select"));
//                    }
//                }

                if (!output.equals(output2)) {
                    if (tag.tagName().equals(tag2.tagName())) {
                        List<String> tagAttributeNames = new ArrayList<>();
                        for (Attribute att : tag.attributes()) {
                            tagAttributeNames.add(att.getKey());
                            // If two attributes are different we flag this as a change
                            if (!att.getValue().equals(tag2.attr(att.getKey()))) {
                                changedElements.addElement(new ChangedElement(tag, tag2, att.getKey(), "attribute"));
                            }
                        }
                        // If the first tag does not have an attribute from the first this is a change
                        for (Attribute att : tag2.attributes()) {
                            if (!tagAttributeNames.contains(att.getKey())) {
                                changedElements.addElement(new ChangedElement(tag, tag2, att.getKey(), "attribute"));
                            }
                        }
                    }
                }

//                if ("input".equals(tag.tag().getName())) {
//                    if (tag.parent() != null && tag2.parent() != null && !tag.parent().text().equals(tag2.parent().text())) {
//                        textElementChanges.add(tag);
//                        textElementChanges2.add(tag2);
//                    }
//                } else {
                if (!tag.text().equals(tag2.text())) {
                    textElementChanges.add(tag);
                    textElementChanges2.add(tag2);
//                    }
                }
            } else {
                Element tag = allElementsMap.get(reference);
                changedElements.addElement(new ChangedElement(tag, tag.html(), null, null, "newElement"));
            }
        }

        // Because calling text returns all elements below as well we only want the lowest level node that has changed.
        textElementChangesFinal.addAll(textElementChanges);
        textElementChangesFinal2.addAll(textElementChanges2);
        for (Element element : textElementChanges) {
            Element parent = element.parent();
            while (parent != null) {
                if (textElementChangesFinal.contains(parent)) {
                    textElementChangesFinal.remove(parent);
                }
                parent = parent.parent();
            }
        }

        // We need to do the same for both lists
        for (Element element : textElementChanges2) {
            Element parent = element.parent();
            while (parent != null) {
                if (textElementChangesFinal2.contains(parent)) {
                    textElementChangesFinal2.remove(parent);
                }
                parent = parent.parent();
            }
        }

        // Once we have found the ones that have really changed we can save only those.
        for (int i = 0; i < textElementChangesFinal.size(); i++) {
//            if ("input".equals(textElementChangesFinal.get(i).tag().getName())) {
//                //changedElements.addElement(new ChangedElement(textElementChangesFinal.get(i), textElementChangesFinal.get(i).parent().text(), textElementChangesFinal2.get(i), textElementChangesFinal2.get(i).parent().text(), "text"));
//            } else {
            changedElements.addElement(new ChangedElement(textElementChangesFinal.get(i), textElementChangesFinal.get(i).text(), textElementChangesFinal2.get(i), textElementChangesFinal2.get(i).text(), "text"));
//            }
        }

        return changedElements;
    }

    public String getStateName() {
        return stateName;
    }

    public SDEElement getElementById(String id) {
        return new SDEElement(doc.getElementById(id));
    }

    public Document getDocument() {
        return doc;
    }
}
