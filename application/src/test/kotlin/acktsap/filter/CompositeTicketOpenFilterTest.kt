package acktsap.filter

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CompositeTicketOpenFilterTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun doFilter_allMatch_returnMatched() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.random()
        val targetTicketOpens = listOf(targetTicketOpen)
        val sut =
            CompositeTicketOpenFilter(
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
            )

        // when
        val actual = sut.doFilter(ticketOpens)

        // then
        assertThat(actual).isEqualTo(targetTicketOpens)
    }

    @Test
    fun doFilter_anyNotMatch_notReturnNotMatched() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.random()
        val targetTicketOpens = listOf(targetTicketOpen)
        val sut =
            CompositeTicketOpenFilter(
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns targetTicketOpens },
                mockk(relaxed = true) { every { doFilter(any()) } returns listOf() },
            )

        // when
        val actual = sut.doFilter(ticketOpens)

        // then
        assertThat(actual).isEmpty()
    }
}
