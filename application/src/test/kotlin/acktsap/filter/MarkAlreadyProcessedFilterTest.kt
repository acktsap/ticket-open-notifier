package acktsap.filter

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test

class MarkAlreadyProcessedFilterTest {
    @Test
    fun doFilterShouldReturnTargetWhichIsNotFilteredOutByExistsMethod() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.random()
        val ticketOpenRepository =
            mockk<ViewedTicketOpenRepository> {
                every { exists(targetTicketOpen) } returns false
                every { exists(not(targetTicketOpen)) } returns true
            }
        val sut = MarkAlreadyProcessedFilter(ticketOpenRepository)

        val actual = sut.doFilter(ticketOpens)

        actual shouldBe listOf(targetTicketOpen)
    }

    @Test
    fun doFilterShouldReturnEmptyWhenAllItemsAreFilteredOutByExistsMethod() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val ticketOpenRepository =
            mockk<ViewedTicketOpenRepository> {
                every { exists(any()) } returns true
            }
        val sut = MarkAlreadyProcessedFilter(ticketOpenRepository)

        val actual = sut.doFilter(ticketOpens)

        actual should beEmpty()
    }
}
