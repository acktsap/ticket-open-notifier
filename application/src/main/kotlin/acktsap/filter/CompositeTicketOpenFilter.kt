package acktsap.filter

import acktsap.model.TicketOpen

class CompositeTicketOpenFilter(
    vararg delegates: TicketOpenFilter,
) : TicketOpenFilter {
    private val delegates = delegates.toList()

    override fun filter(ticketOpen: TicketOpen): Boolean = delegates.any { it.filter(ticketOpen) }
}
