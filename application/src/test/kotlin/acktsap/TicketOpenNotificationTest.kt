package acktsap

import acktsap.detector.TicketOpenDetector
import acktsap.filter.TicketOpenFilter
import acktsap.model.TicketOpen
import acktsap.notifier.TicketOpenNotifier
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

class TicketOpenNotificationTest {
    private val fixtureMonkey =
        FixtureMonkey.builder()
            .plugin(KotlinPlugin())
            .build()

    @Test
    fun run() {
        // given
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val ticketOpenDetector =
            mockk<TicketOpenDetector>(relaxed = true) {
                every { detect() } returns ticketOpens
            }
        val tickerOpenFilter =
            mockk<TicketOpenFilter>(relaxed = true) {
                every { filter(any()) } returns true
            }
        val ticketOpenNotifier = mockk<TicketOpenNotifier>(relaxed = true)
        val ticketOpenNotification =
            TicketOpenNotification(
                ticketOpenDetector = ticketOpenDetector,
                tickerOpenFilter = tickerOpenFilter,
                ticketOpenNotifier = ticketOpenNotifier,
            )

        // when
        ticketOpenNotification.run()

        // then
        verify(exactly = 1) { ticketOpenNotifier.notify(ticketOpens) }
    }
}
