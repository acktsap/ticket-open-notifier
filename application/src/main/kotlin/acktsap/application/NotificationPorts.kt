package acktsap.application

import acktsap.application.port.outgoing.CurrentTime
import acktsap.application.port.outgoing.NotificationHandler
import acktsap.application.port.outgoing.ReadProcessedTickets
import acktsap.application.port.outgoing.TicketDetector
import acktsap.application.port.outgoing.WriteLog
import acktsap.application.port.outgoing.WriteProcessedTickets
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

data class NotificationPorts(
    val read: ReadProcessedTickets,
    val detectors: PersistentList<TicketDetector>,
    val handlers: PersistentList<NotificationHandler>,
    val write: WriteProcessedTickets,
    val now: CurrentTime,
    val log: WriteLog,
) {
    constructor(
        read: ReadProcessedTickets,
        detectors: List<TicketDetector>,
        handlers: List<NotificationHandler>,
        write: WriteProcessedTickets,
        now: CurrentTime,
        log: WriteLog,
    ) : this(read, detectors.toPersistentList(), handlers.toPersistentList(), write, now, log)
}
