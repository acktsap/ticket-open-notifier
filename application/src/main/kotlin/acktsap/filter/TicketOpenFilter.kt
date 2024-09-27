package acktsap.filter

import acktsap.model.TicketOpen

fun interface TicketOpenFilter {
    fun filter(ticketOpen: TicketOpen): Boolean
}
