package acktsap.handler

import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository

class AlreadyProcessedMarkingHandler(
    private val ticketOpenRepository: ViewedTicketOpenRepository,
) : TicketOpenHandler {
    override fun handle(ticketOpens: Collection<TicketOpen>) {
        for (ticketOpen in ticketOpens) {
            ticketOpenRepository.save(ticketOpen)
        }
    }
}
