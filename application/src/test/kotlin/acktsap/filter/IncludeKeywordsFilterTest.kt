package acktsap.filter

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class IncludeKeywordsFilterTest {
    @Test
    fun doFilterShouldNotFilterOutWhenInclusionTargetIsProvided() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.first()
        val sut =
            IncludeKeywordsFilter(
                targetTicketOpen.name.substring(ThreadLocalRandom.current().nextInt(0, targetTicketOpen.name.length)),
                UUID.randomUUID().toString(),
            )

        val actual = sut.doFilter(ticketOpens)

        actual shouldBe listOf(targetTicketOpen)
    }

    @Test
    fun doFilterShouldFilterOutWhenNoMatchingInclusionTargetIsProvided() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val sut = IncludeKeywordsFilter(UUID.randomUUID().toString(), UUID.randomUUID().toString())

        val actual = sut.doFilter(ticketOpens)

        actual should beEmpty()
    }
}
