package acktsap.filter

import acktsap.model.TicketOpen

fun interface TicketOpenFilter {
    /**
     * @return true if it should be filtered
     */
    fun filter(ticketOpen: TicketOpen): Boolean
}
