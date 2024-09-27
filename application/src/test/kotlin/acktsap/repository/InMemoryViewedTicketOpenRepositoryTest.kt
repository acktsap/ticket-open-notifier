package acktsap.repository

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.function.Consumer

class InMemoryViewedTicketOpenRepositoryTest {
    private val fixtureMonkey =
        FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun save_and_exits() {
        // given
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val sut = InMemoryViewedTicketOpenRepository(10)

        // when
        sut.save(ticketOpen)
        val actual = sut.exists(ticketOpen)

        // then
        assertThat(actual).isTrue()
    }

    @Test
    fun exists_notExists_false() {
        // given
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val sut = InMemoryViewedTicketOpenRepository(10)

        // when
        val actual = sut.exists(ticketOpen)

        // then
        assertThat(actual).isFalse()
    }

    @Test
    fun save_moreThenCapacity_remainsNewValues() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val sut = InMemoryViewedTicketOpenRepository(3)

        // when
        for (ticketOpen in ticketOpens) {
            sut.save(ticketOpen)
        }

        // then
        assertThat(ticketOpens.slice(0..6)).allSatisfy(
            Consumer {
                assertThat(sut.exists(it)).isFalse()
            },
        )
        assertThat(ticketOpens.slice(7..9)).allSatisfy(
            Consumer {
                assertThat(sut.exists(it)).isTrue()
            },
        )
    }
}
