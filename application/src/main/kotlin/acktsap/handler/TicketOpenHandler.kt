package acktsap.handler

import acktsap.model.TicketOpen

fun interface TicketOpenHandler {
    fun handle(ticketOpens: Collection<TicketOpen>)
}
