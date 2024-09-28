package acktsap.filter

import acktsap.model.TicketOpen

class CompositeTicketOpenFilter(
    vararg delegates: TicketOpenFilter,
) : TicketOpenFilter {
    private val delegates = delegates.toList()

    override fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen> =
        delegates.fold(ticketOpens) { remains, filter ->
            filter.doFilter(remains)
        }
}
