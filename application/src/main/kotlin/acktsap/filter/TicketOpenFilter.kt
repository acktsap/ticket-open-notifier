package acktsap.filter

import acktsap.model.TicketOpen

fun interface TicketOpenFilter {
    fun filter(ticketOpens: TicketOpen): Boolean
}
