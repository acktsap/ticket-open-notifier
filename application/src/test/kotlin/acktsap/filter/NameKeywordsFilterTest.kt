package acktsap.filter

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
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
    fun doFilter_existsOnTarget_returnTarget() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val targetTicketOpen = ticketOpens.first()
        val sut =
            NameKeywordsFilter(
                targetTicketOpen.name.substring(ThreadLocalRandom.current().nextInt(0, 10)),
                UUID.randomUUID().toString(),
            )

        // when
        val actual = sut.doFilter(ticketOpens)

        // then
        assertThat(actual).isEqualTo(listOf(targetTicketOpen))
    }

    @Test
    fun doFilter_notExistsForAll_returnEmptyList() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val sut = NameKeywordsFilter(UUID.randomUUID().toString(), UUID.randomUUID().toString())

        // when
        val actual = sut.doFilter(ticketOpens)

        // then
        assertThat(actual).isEmpty()
    }
}
