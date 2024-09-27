package acktsap.handler

import acktsap.model.TicketOpen

class CompositeHandler(
    private val delegates: List<TicketOpenHandler>,
) : TicketOpenHandler {
    override fun handle(ticketOpens: Collection<TicketOpen>) {
        for (delegate in delegates) {
            delegate.handle(ticketOpens)
        }
    }
}
