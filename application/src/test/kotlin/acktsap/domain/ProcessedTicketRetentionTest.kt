package acktsap.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import java.time.LocalDateTime
import java.time.ZoneOffset

class ProcessedTicketRetentionTest : StringSpec({
    "retainRecentTicketsShouldKeepOnlyEntriesAfterTheCutoff" {
        checkAll(Arb.long(-10_000_000L..10_000_000L)) { epochSecond ->
            val ticket = TicketOpen("show", Platform.INTERPARK, LocalDateTime.ofEpochSecond(epochSecond, 0, ZoneOffset.UTC))

            val after = retainRecentTickets(setOf(ticket), ticket.dateTime.minusNanos(1))
            val atCutoff = retainRecentTickets(setOf(ticket), ticket.dateTime)

            after shouldBe setOf(ticket)
            atCutoff shouldBe emptySet()
        }
    }
})
