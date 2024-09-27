package acktsap.repository

import acktsap.model.TicketOpen

interface ViewedTicketOpenRepository {
    fun save(ticketOpen: TicketOpen)

    fun exists(ticketOpen: TicketOpen): Boolean
}
