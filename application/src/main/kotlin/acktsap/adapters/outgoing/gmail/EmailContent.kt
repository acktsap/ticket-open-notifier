package acktsap.adapters.outgoing.gmail

import acktsap.domain.TicketOpen

data class EmailContent(
    val title: String,
    val body: String,
)

fun renderEmail(ticketOpens: List<TicketOpen>): EmailContent =
    EmailContent(
        title = "티켓 오픈 알람 (${ticketOpens.size}개)",
        body =
            ticketOpens
                .sortedBy { it.dateTime }
                .joinToString(separator = "\n\n") {
                    "${it.dateTime.toLocalDate()} ${it.dateTime.toLocalTime()}\n${it.name} ${it.platform}"
                },
    )
