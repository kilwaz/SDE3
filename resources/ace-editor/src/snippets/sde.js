define("ace/snippets/sde", ["require", "exports", "module"], function (require, exports, module) {
    "use strict";

    exports.snippetText = "# Shebang. Executing bash via /usr/bin/env makes scripts more portable.\n\
snippet #!\n\
	#!/usr/bin/env bash\n\
	\n\
	";

    exports.scope = "sde";
});