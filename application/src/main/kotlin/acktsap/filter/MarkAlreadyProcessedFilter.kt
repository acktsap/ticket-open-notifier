package acktsap.filter

import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository

class MarkAlreadyProcessedFilter(
    private val ticketOpenRepository: ViewedTicketOpenRepository,
) : TicketOpenFilter {
    override fun filter(ticketOpen: TicketOpen): Boolean = ticketOpenRepository.exists(ticketOpen)
}
