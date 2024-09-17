package acktsap.detector

import acktsap.model.TicketOpen

interface TicketOpenDetector {
    fun detect(): List<TicketOpen>
}
