package acktsap

import acktsap.adapters.incoming.cli.applicationProgram
import acktsap.adapters.incoming.cli.runApplication
import acktsap.adapters.outgoing.configuration.systemSettings
import acktsap.adapters.outgoing.console.systemConsole
import acktsap.adapters.outgoing.file.readProcessedTickets
import acktsap.adapters.outgoing.file.writeProcessedTickets
import acktsap.adapters.outgoing.gmail.systemGmailHandler
import acktsap.adapters.outgoing.interpark.detectInterparkTicketOpens
import acktsap.adapters.outgoing.runtime.systemClock
import acktsap.adapters.outgoing.runtime.systemLog
import acktsap.application.NotificationPorts
import acktsap.bootstrap.prepareNotification
import acktsap.configuration.MissingSettings
import acktsap.fp.Either
import acktsap.fp.IO
import java.time.ZoneId
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val console = systemConsole()
    val prepare = prepareNotification(systemSettings()) { livePorts() }
    val program = applicationProgram(args.toList(), console, prepare)
    val exitCode = runApplication(program, console).unsafeRunSync()
    if (exitCode != 0) exitProcess(exitCode)
}

private fun livePorts(): IO<Either<MissingSettings, NotificationPorts>> =
    systemGmailHandler().map { configured ->
        configured.map { handler ->
            NotificationPorts(
                read = ::readProcessedTickets,
                detectors = listOf(::detectInterparkTicketOpens),
                handlers = listOf(handler),
                write = ::writeProcessedTickets,
                now = systemClock(ZoneId.of("Asia/Seoul")),
                log = systemLog(),
            )
        }
    }
