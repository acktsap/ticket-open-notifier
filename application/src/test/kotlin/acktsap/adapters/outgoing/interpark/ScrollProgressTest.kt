package acktsap.adapters.outgoing.interpark

import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.checkAll
import java.time.LocalDateTime

class ScrollProgressTest : StringSpec({
    "recordShouldKeepFirstSeenOrderAndStopAfterThreeUnchangedPages" {
        checkAll(Arb.list(Arb.int(), 0..60)) { ids ->
            val time = LocalDateTime.of(2026, 10, 30, 12, 0)
            val detected = ids.map { TicketOpen("ticket-$it", Platform.INTERPARK, time) }
            val initial = ScrollProgress().record(detected)
            val twice = initial.record(detected).record(detected)
            val complete = twice.record(detected)

            initial.tickets shouldBe detected.distinct()
            initial.complete shouldBe false
            twice.complete shouldBe ids.isEmpty()
            complete.complete shouldBe true
            complete.tickets shouldBe detected.distinct()

            val fresh = TicketOpen("fresh", Platform.INTERPARK, time)
            val resumed = complete.record(listOf(fresh, fresh))
            resumed.tickets shouldBe detected.distinct() + fresh
            resumed.unchangedScrolls shouldBe 0
            resumed.complete shouldBe false
            complete.tickets shouldBe detected.distinct()
        }
    }
})
