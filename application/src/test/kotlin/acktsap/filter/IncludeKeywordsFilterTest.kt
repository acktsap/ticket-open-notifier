package acktsap.filter

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class IncludeKeywordsFilterTest {
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
            IncludeKeywordsFilter(
                targetTicketOpen.name.substring(ThreadLocalRandom.current().nextInt(0, targetTicketOpen.name.length)),
                UUID.randomUUID().toString(),
            )

        // when
        val actual = sut.doFilter(ticketOpens)

        // then
        actual shouldBe listOf(targetTicketOpen)
    }

    @Test
    fun doFilter_notExistsForAll_returnEmptyList() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val sut = IncludeKeywordsFilter(UUID.randomUUID().toString(), UUID.randomUUID().toString())

        // when
        val actual = sut.doFilter(ticketOpens)

        // then
        actual should beEmpty()
    }
}
