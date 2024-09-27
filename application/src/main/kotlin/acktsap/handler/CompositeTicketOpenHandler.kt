package acktsap.handler

import acktsap.model.TicketOpen

class CompositeTicketOpenHandler(
    vararg delegates: TicketOpenHandler,
) : TicketOpenHandler {
    private val delegates = delegates.toList()

    override fun handle(ticketOpens: Collection<TicketOpen>) {
        for (delegate in delegates) {
            delegate.handle(ticketOpens)
        }
    }
}
