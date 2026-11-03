package acktsap.domain

import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentSet
import java.time.LocalDateTime

fun retainRecentTickets(
    tickets: Set<TicketOpen>,
    retainAfter: LocalDateTime,
): PersistentSet<TicketOpen> = tickets.filter { it.dateTime.isAfter(retainAfter) }.toPersistentSet()
