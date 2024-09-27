package acktsap.filter

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.set
import net.jqwik.api.Arbitraries
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class NameKeywordsFilterTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun filter_exists_true() {
        // given
        val ticketOpen =
            fixtureMonkey
                .giveMeBuilder<TicketOpen>()
                .set(TicketOpen::name, Arbitraries.strings().ofMinLength(10))
                .sample()

        val sut =
            NameKeywordsFilter(
                ticketOpen.name.substring(ThreadLocalRandom.current().nextInt(0, 10)),
                UUID.randomUUID().toString(),
            )

        // when
        val actual = sut.filter(ticketOpen)

        // then
        assertThat(actual).isTrue()
    }

    @Test
    fun filter_notExists_false() {
        // given
        val ticketOpen =
            fixtureMonkey
                .giveMeBuilder<TicketOpen>()
                .set(TicketOpen::name, Arbitraries.strings().ofMinLength(10))
                .sample()

        val sut = NameKeywordsFilter(UUID.randomUUID().toString(), UUID.randomUUID().toString())

        // when
        val actual = sut.filter(ticketOpen)

        // then
        assertThat(actual).isFalse()
    }
}
