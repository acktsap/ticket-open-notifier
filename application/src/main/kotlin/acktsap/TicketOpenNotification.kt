package acktsap

import acktsap.detector.TicketOpenDetector
import acktsap.filter.TicketOpenFilter
import acktsap.notifier.TicketOpenNotifier

class TicketOpenNotification(
    private val ticketOpenDetector: TicketOpenDetector,
    private val tickerOpenFilter: TicketOpenFilter,
    private val ticketOpenNotifier: TicketOpenNotifier,
) {
    fun run() {
        val tickerOpens =
            ticketOpenDetector.detect()
                .filter { tickerOpenFilter.filter(it) }
        ticketOpenNotifier.notify(tickerOpens)
    }
}
