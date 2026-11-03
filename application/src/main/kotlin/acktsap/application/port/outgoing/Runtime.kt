package acktsap.application.port.outgoing

import acktsap.fp.IO
import java.time.LocalDateTime

typealias CurrentTime = IO<LocalDateTime>

typealias WriteLog = (String) -> IO<Unit>
