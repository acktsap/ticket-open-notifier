package acktsap.adapters.outgoing.console

import acktsap.adapters.incoming.cli.Console
import acktsap.fp.IO

fun systemConsole(): Console =
    Console(
        printLine = { text -> IO.delay { println(text) } },
        printError = { text -> IO.delay { System.err.println(text) } },
    )
