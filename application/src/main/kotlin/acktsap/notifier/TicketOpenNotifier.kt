package acktsap.notifier

import acktsap.model.TicketOpen

fun interface TicketOpenNotifier {
    fun notify(ticketOpens: Collection<TicketOpen>)
}
