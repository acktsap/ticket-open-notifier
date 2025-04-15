package acktsap.filter

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class CompositeTicketOpenFilterTest {
    @Test
    fun doFilterShouldReturnTargetWhenAllMatched() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.random()
        val targetTicketOpens = listOf(targetTicketOpen)
        val sut =
            CompositeTicketOpenFilter(
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
            )

        val actual = sut.doFilter(ticketOpens)

        actual shouldBe targetTicketOpens
    }

    @Test
    fun doFilterShouldNotReturnTargetWhenThereAreNotMatchedOne() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.random()
        val targetTicketOpens = listOf(targetTicketOpen)
        val sut =
            CompositeTicketOpenFilter(
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns listOf() },
            )

        val actual = sut.doFilter(ticketOpens)

        actual should beEmpty()
    }
}
