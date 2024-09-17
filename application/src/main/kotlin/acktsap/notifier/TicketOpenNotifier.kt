package acktsap.notifier

import acktsap.model.TicketOpen

interface TicketOpenNotifier {
    fun notify(ticketOpens: Collection<TicketOpen>)
}
