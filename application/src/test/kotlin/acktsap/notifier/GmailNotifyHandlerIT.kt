package acktsap.notifier

import acktsap.handler.GmailNotifyHandler
import acktsap.model.Platform
import acktsap.model.TicketOpen
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class GmailNotifyHandlerIT {
    @Disabled("Manual test")
    @Test
    fun handle() {
        // given
        val ticketOpens =
            listOf(
                TicketOpen(
                    name = "테스트 오픈1",
                    platform = Platform.INTERPARK,
                    dateTime = LocalDateTime.now(),
                ),
                TicketOpen(
                    name = "테스트 오픈2",
                    platform = Platform.INTERPARK,
                    dateTime = LocalDateTime.now().plusDays(2),
                ),
            )
        val sut =
            GmailNotifyHandler(
                username = "sibera21@gmail.com",
                // check pw in https://myaccount.google.com/apppasswords
                password = "todo",
                recipients =
                    listOf(
                        "sibera21@gmail.com",
                    ),
            )

        // when
        sut.handle(ticketOpens)

        // then
        // check if sent to your mail
    }
}
