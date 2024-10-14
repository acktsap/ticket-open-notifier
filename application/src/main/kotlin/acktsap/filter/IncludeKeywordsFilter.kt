package acktsap.filter

import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class IncludeKeywordsFilter(
    vararg keywords: String,
) : TicketOpenFilter {
    private val keywords: Set<String> = keywords.toSet()

    override fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen> {
        val filtered =
            ticketOpens.filter { ticketOpen ->
                keywords.any { ticketOpen.name.contains(it) }
            }
        logger.debug {
            "${filtered.size} items remains after include keyword filtering (keywords: ${keywords.joinToString { "," }})"
        }
        return filtered
    }
}
