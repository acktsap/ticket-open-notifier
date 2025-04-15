package acktsap.handler

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import acktsap.repository.ViewedTicketOpenRepository
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test

class AlreadyProcessedMarkingHandlerTest {
    @Test
    fun handleShouldInvokeSaveForAllTickOpens() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val ticketOpenRepository = mockk<ViewedTicketOpenRepository>(relaxed = true)
        val sut = AlreadyProcessedMarkingHandler(ticketOpenRepository)

        sut.handle(ticketOpens)

        verifyAll {
            ticketOpens.forEach {
                ticketOpenRepository.save(it)
            }
        }
    }
}
