package acktsap.repository

import acktsap.model.TicketOpen

class InMemoryViewedTicketOpenRepository(
    capacity: Int,
) : ViewedTicketOpenRepository {
    private val viewedSet =
        object : LinkedHashMap<TicketOpen, Boolean>(capacity, 0.75F, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<TicketOpen, Boolean>): Boolean = size > capacity
        }

    override fun save(ticketOpen: TicketOpen) {
        viewedSet[ticketOpen] = true
    }

    override fun exists(ticketOpen: TicketOpen): Boolean = viewedSet.containsKey(ticketOpen)

    override fun close() {
        // do nothing
    }
}
