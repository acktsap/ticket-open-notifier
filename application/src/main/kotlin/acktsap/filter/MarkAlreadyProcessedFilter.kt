package acktsap.filter

import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class MarkAlreadyProcessedFilter(
    private val ticketOpenRepository: ViewedTicketOpenRepository,
) : TicketOpenFilter {
    override fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen> {
        val filtered =
            ticketOpens.filter {
                !ticketOpenRepository.exists(it)
            }
        logger.debug { "${filtered.size} items remains after visited filtering" }
        return filtered
    }
}
