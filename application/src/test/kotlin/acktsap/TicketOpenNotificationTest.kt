package acktsap

import acktsap.detector.TicketOpenDetector
import acktsap.filter.TicketOpenFilter
import acktsap.handler.TicketOpenHandler
import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TicketOpenNotificationTest {
    @Test
    fun runShouldInvokeHandleWhenItIsDetectedAndNotFilteredOut() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val ticketOpenDetector =
            mockk<TicketOpenDetector>(relaxed = true) {
                every { detect() } returns ticketOpens
            }
        val targetTicketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val tickerOpenFilter =
            mockk<TicketOpenFilter>(relaxed = true) {
                every { doFilter(ticketOpens) } returns targetTicketOpens
            }
        val ticketOpenHandler = mockk<TicketOpenHandler>(relaxed = true)
        val ticketOpenNotification =
            TicketOpenNotification(
                ticketOpenDetector = ticketOpenDetector,
                ticketOpenFilter = tickerOpenFilter,
                ticketOpenHandler = ticketOpenHandler,
            )

        ticketOpenNotification.run()

        verify(exactly = 1) { ticketOpenHandler.handle(targetTicketOpens) }
    }
}
