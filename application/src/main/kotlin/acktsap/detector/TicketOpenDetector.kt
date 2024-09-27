package acktsap.detector

import acktsap.model.TicketOpen

fun interface TicketOpenDetector {
    fun detect(): List<TicketOpen>
}
