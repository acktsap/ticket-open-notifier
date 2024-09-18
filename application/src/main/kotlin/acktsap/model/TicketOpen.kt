package acktsap.model

import java.time.LocalDateTime

data class TicketOpen(
    val name: String,
    val platform: Platform,
    // in kst
    val dateTime: LocalDateTime,
)
