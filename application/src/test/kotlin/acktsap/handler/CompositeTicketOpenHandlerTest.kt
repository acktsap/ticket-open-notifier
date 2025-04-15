package acktsap.handler

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.mockk.mockk
import io.mockk.verifyAll
import org.junit.jupiter.api.Test

class CompositeTicketOpenHandlerTest {
    @Test
    fun handleShouldInvokeHandleForAllDelegates() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val delegates =
            listOf<TicketOpenHandler>(
                mockk(relaxed = true),
                mockk(relaxed = true),
            )
        val sut =
            CompositeTicketOpenHandler(
                *delegates.toTypedArray(),
            )

        sut.handle(ticketOpens)

        verifyAll {
            delegates.forEach {
                it.handle(ticketOpens)
            }
        }
    }
}
