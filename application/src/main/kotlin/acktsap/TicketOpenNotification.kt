package acktsap

import acktsap.detector.TicketOpenDetector
import acktsap.filter.TicketOpenFilter
import acktsap.handler.TicketOpenHandler
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class TicketOpenNotification(
    private val ticketOpenDetector: TicketOpenDetector,
    private val ticketOpenFilter: TicketOpenFilter,
    private val ticketOpenHandler: TicketOpenHandler,
) {
    fun run() {
        val ticketOpens = ticketOpenDetector.detect()
        logger.info { "${ticketOpens.size} ticketOpens detected" }
        ticketOpens.forEach {
            logger.debug { "-- $it" }
        }

        val targetTickerOpens = ticketOpenFilter.doFilter(ticketOpens)
        logger.info { "${targetTickerOpens.size} ticketOpens remains after filtering" }
        targetTickerOpens.forEach {
            logger.debug { "-- $it" }
        }

        ticketOpenHandler.handle(targetTickerOpens)
    }
}
