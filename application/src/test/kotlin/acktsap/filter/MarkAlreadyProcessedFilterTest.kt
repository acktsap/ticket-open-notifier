package acktsap.filter

import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MarkAlreadyProcessedFilterTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun filter_repositoryReturnTrue_true() {
        // given
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val ticketOpenRepository =
            mockk<ViewedTicketOpenRepository> {
                every { exists(any()) } returns true
            }
        val sut = MarkAlreadyProcessedFilter(ticketOpenRepository)

        // when
        val actual = sut.filter(ticketOpen)

        // then
        assertThat(actual).isTrue()
    }

    @Test
    fun filter_repositoryReturnFalse_false() {
        // given
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val ticketOpenRepository =
            mockk<ViewedTicketOpenRepository> {
                every { exists(any()) } returns false
            }
        val sut = MarkAlreadyProcessedFilter(ticketOpenRepository)

        // when
        val actual = sut.filter(ticketOpen)

        // then
        assertThat(actual).isFalse()
    }
}
