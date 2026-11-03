package acktsap.domain

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

data class NotificationPlan(
    val detected: PersistentList<TicketOpen>,
    val selected: PersistentList<TicketOpen>,
) {
    constructor(detected: List<TicketOpen>, selected: List<TicketOpen>) :
        this(detected.toPersistentList(), selected.toPersistentList())
}
