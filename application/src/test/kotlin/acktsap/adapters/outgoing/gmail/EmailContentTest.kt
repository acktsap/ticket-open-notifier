package acktsap.adapters.outgoing.gmail

import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.time.LocalDateTime

class EmailContentTest : StringSpec({
    "renderEmailShouldNotDependOnInputOrderWhenOpeningTimesAreDistinct" {
        checkAll(Arb.list(Arb.string(0..40), 0..30)) { names ->
            val time = LocalDateTime.of(2026, 1, 1, 0, 0)
            val tickets = names.mapIndexed { index, name -> TicketOpen(name, Platform.INTERPARK, time.plusSeconds(index.toLong())) }

            val actual = renderEmail(tickets.reversed())

            actual shouldBe renderEmail(tickets)
        }
    }
})
