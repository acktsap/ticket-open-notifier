package acktsap.repository

import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

class InMemoryViewedTicketOpenRepository(
    capacity: Int,
) : ViewedTicketOpenRepository {
    private val viewedSet =
        object : LinkedHashMap<TicketOpen, Boolean>(capacity, 0.75F, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<TicketOpen, Boolean>): Boolean {
                val shouldRemoved = size > capacity
                if (shouldRemoved) {
                    logger.info { "Size exceed capacity (size: $size, capacity: $capacity). Remove eldest entry." }
                }
                return shouldRemoved
            }
        }

    override fun save(ticketOpen: TicketOpen) {
        viewedSet[ticketOpen] = true
    }

    override fun exists(ticketOpen: TicketOpen): Boolean = viewedSet.containsKey(ticketOpen)

    override fun close() {
        // do nothing
    }
}
