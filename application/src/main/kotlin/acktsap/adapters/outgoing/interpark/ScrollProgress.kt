package acktsap.adapters.outgoing.interpark

import acktsap.domain.TicketOpen
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentSet

internal data class ScrollProgress(
    val tickets: PersistentList<TicketOpen> = persistentListOf(),
    val unchangedScrolls: Int = 0,
) {
    val complete: Boolean get() = unchangedScrolls >= 3

    fun record(detected: List<TicketOpen>): ScrollProgress {
        val known = tickets.toPersistentSet()
        val collected = tickets.addAll(detected.distinct().filterNot { it in known })
        return ScrollProgress(collected, if (collected.size == tickets.size) unchangedScrolls + 1 else 0)
    }
}
