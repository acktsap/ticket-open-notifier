package acktsap.handler

import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test

class AlreadyProcessedMarkingHandlerTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun handle() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val ticketOpenRepository = mockk<ViewedTicketOpenRepository>(relaxed = true)
        val sut = AlreadyProcessedMarkingHandler(ticketOpenRepository)

        // when
        sut.handle(ticketOpens)

        // then
        verifyAll {
            ticketOpens.forEach {
                ticketOpenRepository.save(it)
            }
        }
    }
}
