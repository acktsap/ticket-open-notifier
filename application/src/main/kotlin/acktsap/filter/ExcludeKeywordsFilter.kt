package acktsap.filter

import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class ExcludeKeywordsFilter(
    vararg keywords: String,
) : TicketOpenFilter {
    private val keywords: Set<String> = keywords.toSet()

    override fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen> {
        val filtered =
            ticketOpens.filterNot { ticketOpen ->
                keywords.any { ticketOpen.name.contains(it) }
            }
        logger.debug {
            "${filtered.size} items remains after exclude keyword filtering (keywords: ${keywords.joinToString { "," }})"
        }
        return filtered
    }
}
