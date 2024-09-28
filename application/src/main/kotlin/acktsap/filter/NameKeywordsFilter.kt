package acktsap.filter

import acktsap.model.TicketOpen

class NameKeywordsFilter(
    vararg keywords: String,
) : TicketOpenFilter {
    private val keywords: List<String> = keywords.toList()

    override fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen> =
        ticketOpens.filter { ticketOpen ->
            keywords.any { ticketOpen.name.contains(it) }
        }
}
