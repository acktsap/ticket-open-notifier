package acktsap.domain

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet

data class TicketOpenRules(
    val includeKeywords: PersistentSet<String>,
    val excludeKeywords: PersistentSet<String>,
) {
    constructor(includeKeywords: Set<String>, excludeKeywords: Set<String>) :
        this(includeKeywords.toPersistentSet(), excludeKeywords.toPersistentSet())
}

fun TicketOpenRules.matches(ticket: TicketOpen): Boolean =
    (includeKeywords.isEmpty() || includeKeywords.any { ticket.name.contains(it) }) &&
        excludeKeywords.none { ticket.name.contains(it) }

fun selectTicketOpens(
    detected: List<TicketOpen>,
    alreadyProcessed: Set<TicketOpen>,
    rules: TicketOpenRules,
): PersistentList<TicketOpen> = detected.filter { ticket -> ticket !in alreadyProcessed && rules.matches(ticket) }.toPersistentList()
