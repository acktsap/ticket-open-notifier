package acktsap

import acktsap.detector.TicketOpenDetector
import acktsap.filter.TicketOpenFilter
import acktsap.handler.TicketOpenHandler

class TicketOpenNotification(
    private val ticketOpenDetector: TicketOpenDetector,
    private val tickerOpenFilter: TicketOpenFilter,
    private val ticketOpenHandler: TicketOpenHandler,
) {
    fun run() {
        val tickerOpens =
            ticketOpenDetector.detect()
                .filter { tickerOpenFilter.filter(it) }
        ticketOpenHandler.handle(tickerOpens)
    }
}
