package acktsap.filter

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeOne
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
    fun filter_anyReturnTrue_returnTrue() {
        // given
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val sut =
            CompositeTicketOpenFilter(
                mockk(relaxed = true) { every { filter(any()) } returns true },
                mockk(relaxed = true) { every { filter(any()) } returns false },
                mockk(relaxed = true) { every { filter(any()) } returns false },
            )

        // when
        val actual = sut.filter(ticketOpen)

        // then
        assertThat(actual).isTrue()
    }

    @Test
    fun filter_allReturnFalse_returnFalse() {
        // given
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val sut =
            CompositeTicketOpenFilter(
                mockk(relaxed = true) { every { filter(any()) } returns false },
                mockk(relaxed = true) { every { filter(any()) } returns false },
                mockk(relaxed = true) { every { filter(any()) } returns false },
            )

        // when
        val actual = sut.filter(ticketOpen)

        // then
        assertThat(actual).isFalse()
    }
}
