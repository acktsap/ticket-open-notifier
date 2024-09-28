package acktsap.filter

import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository

class MarkAlreadyProcessedFilter(
    private val ticketOpenRepository: ViewedTicketOpenRepository,
) : TicketOpenFilter {
    override fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen> =
        ticketOpens.filter {
            !ticketOpenRepository.exists(it)
        }
}
