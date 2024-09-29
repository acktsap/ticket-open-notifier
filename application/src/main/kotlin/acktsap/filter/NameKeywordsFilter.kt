package acktsap.filter

import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class NameKeywordsFilter(
    vararg keywords: String,
) : TicketOpenFilter {
    private val keywords: List<String> = keywords.toList()

    override fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen> {
        val filtered =
            ticketOpens.filter { ticketOpen ->
                keywords.any { ticketOpen.name.contains(it) }
            }
        logger.debug {
            "${filtered.size} items remains after keyword filtering (keywords: ${keywords.joinToString { "," }})"
        }
        return filtered
    }
}
