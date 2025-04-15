package acktsap.filter

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class ExcludeKeywordsFilterTest {
    @Test
    fun doFilterShouldFilterOutWhenExclusionTargetIsProvided() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.first()
        val sut =
            ExcludeKeywordsFilter(
                targetTicketOpen.name.substring(ThreadLocalRandom.current().nextInt(0, targetTicketOpen.name.length)),
                UUID.randomUUID().toString(),
            )

        val actual = sut.doFilter(ticketOpens)

        actual shouldBe (ticketOpens - targetTicketOpen)
    }

    @Test
    fun doFilterShouldNotFilterOutWhenNoMatchingExclusionTargetIsProvided() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val sut = ExcludeKeywordsFilter(UUID.randomUUID().toString(), UUID.randomUUID().toString())

        val actual = sut.doFilter(ticketOpens)

        actual shouldBe ticketOpens
    }
}
