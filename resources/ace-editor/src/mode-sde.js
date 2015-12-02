define("ace/mode/sde_highlight_rules", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text_highlight_rules"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextHighlightRules = require("./text_highlight_rules").TextHighlightRules;

    var GitignoreHighlightRules = function () {

        this.$rules = {
            "start": [
                {
                    token: "comment",
                    regex: /^\s*(\/\/).*$/
                },
                {
                    token: "constant.numeric",
                    regex: /::/
                },
                {
                    token: "variable",
                    regex: /(if|url|wait|frame|loop|exit|click|input|track|run|set|log|call|function|end|select|javascript|driver)>/
                }
            ]
        };

        oop.inherits(GitignoreHighlightRules, TextHighlightRules);

        exports.GitignoreHighlightRules = GitignoreHighlightRules;

        //this.normalizeRules();
    };

    GitignoreHighlightRules.metaData = {
        fileTypes: ['gitignore'],
        name: 'Gitignore'
    };

    oop.inherits(GitignoreHighlightRules, TextHighlightRules);

    exports.GitignoreHighlightRules = GitignoreHighlightRules;
});

define("ace/mode/sde", ["require", "exports", "module", "ace/lib/oop", "ace/mode/text", "ace/mode/sde_highlight_rules"], function (require, exports, module) {
    "use strict";

    var oop = require("../lib/oop");
    var TextMode = require("./text").Mode;
    var GitignoreHighlightRules = require("./sde_highlight_rules").GitignoreHighlightRules;

    var Mode = function () {
        this.HighlightRules = GitignoreHighlightRules;
    };
    oop.inherits(Mode, TextMode);

    (function () {
        this.$id = "ace/mode/sde";
    }).call(Mode.prototype);

    exports.Mode = Mode;
});
