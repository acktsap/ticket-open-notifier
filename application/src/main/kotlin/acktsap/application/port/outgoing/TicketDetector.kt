package acktsap.application.port.outgoing

import acktsap.domain.TicketOpen
import acktsap.fp.IO

typealias TicketDetector = () -> IO<List<TicketOpen>>
