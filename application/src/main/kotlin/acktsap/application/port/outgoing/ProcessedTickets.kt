package acktsap.application.port.outgoing

import acktsap.domain.TicketOpen
import acktsap.fp.IO
import java.nio.file.Path

typealias ReadProcessedTickets = (Path) -> IO<Set<TicketOpen>>

typealias WriteProcessedTickets = (Path, Set<TicketOpen>) -> IO<Unit>
