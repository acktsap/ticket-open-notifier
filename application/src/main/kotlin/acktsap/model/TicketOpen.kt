package acktsap.model

import java.time.LocalDate

data class TicketOpen(
    val name: String,
    val platform: Platform,
    // in kst
    val date: LocalDate,
)
