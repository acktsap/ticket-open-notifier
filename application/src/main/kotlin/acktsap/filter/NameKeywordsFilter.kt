package acktsap.filter

import acktsap.model.TicketOpen

class NameKeywordsFilter(
    vararg keywords: String,
) : TicketOpenFilter {
    private val keywords: List<String> = keywords.toList()

    override fun filter(ticketOpen: TicketOpen): Boolean {
        return keywords.any { ticketOpen.name.contains(it) }
    }
}
