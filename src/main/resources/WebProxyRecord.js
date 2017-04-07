processElements("click", ["input", "a"]);
processElements("change", ["input"]);

function processElements(action, elementTags) {
    for (var tagLoop = 0; tagLoop < elementTags.length; tagLoop++) {

        var elements = document.getElementsByTagName(elementTags[tagLoop]);
        for (var elementLoop = 0; elementLoop < elements.length; elementLoop++) {
            elements[elementLoop].addEventListener(action, function (event) {
                var elementTag = this.tagName;
                var elementId = this.id;
                var elementValue = this.value;
                var eventAction = event.type;

                var xmlHttp = new XMLHttpRequest();
                xmlHttp.open("GET", "http://localhost:10001/record?" +
                "action=" + eventAction +
                "&tag=" + elementTag +
                "&id=" + elementId +
                "&value=" + elementValue, false);

                xmlHttp.send(null);
            }, false);
        }
    }
}




