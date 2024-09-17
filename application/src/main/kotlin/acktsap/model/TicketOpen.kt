package acktsap.model

import java.time.LocalDate

data class TicketOpen(
    val name: String,
    // in kst
    val date: LocalDate,
)
