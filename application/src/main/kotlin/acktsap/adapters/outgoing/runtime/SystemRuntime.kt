package acktsap.adapters.outgoing.runtime

import acktsap.application.port.outgoing.CurrentTime
import acktsap.application.port.outgoing.WriteLog
import acktsap.fp.IO
import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.LocalDateTime
import java.time.ZoneId

fun systemClock(zone: ZoneId): CurrentTime = IO.delay { LocalDateTime.now(zone) }

fun systemLog(): WriteLog = { message -> IO.delay { KotlinLogging.logger { }.info { message } } }
