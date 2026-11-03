package acktsap.adapters.incoming.cli

import acktsap.fp.IO

data class Console(
    val printLine: (String) -> IO<Unit>,
    val printError: (String) -> IO<Unit>,
)
