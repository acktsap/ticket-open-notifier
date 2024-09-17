package acktsap.filter

import acktsap.model.TicketOpen

interface TicketOpenFilter {
    fun filter(ticketOpens: TicketOpen): Boolean
}
