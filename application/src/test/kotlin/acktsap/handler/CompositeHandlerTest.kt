package acktsap.handler

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test

class CompositeHandlerTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun handle() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val delegates =
            listOf<TicketOpenHandler>(
                mockk(relaxed = true),
                mockk(relaxed = true),
            )
        val sut =
            CompositeHandler(
                delegates,
            )

        // when
        sut.handle(ticketOpens)

        // then
        verifyAll {
            delegates.forEach {
                it.handle(ticketOpens)
            }
        }
    }
}
