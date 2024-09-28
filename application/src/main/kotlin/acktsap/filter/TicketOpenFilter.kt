package acktsap.filter

import acktsap.model.TicketOpen

fun interface TicketOpenFilter {
    fun doFilter(ticketOpens: List<TicketOpen>): List<TicketOpen>
}
