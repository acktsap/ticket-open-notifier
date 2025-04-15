package acktsap.notifier

import acktsap.handler.GmailNotifyHandler
import acktsap.model.Platform
import acktsap.model.TicketOpen
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class GmailNotifyHandlerTest {
    @Nested
    inner class UnitTest {
        @Disabled("manual test")
        @Test
        fun buildContent() {
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

            val actual = sut.buildContent(ticketOpens)

            // (manual check)
            println(actual)
        }

        @Test
        fun handleShouldDoNothingWhenRecipientIsEmpty() {
            val ticketOpens = listOf<TicketOpen>()
            val sut =
                GmailNotifyHandler(
                    username = UUID.randomUUID().toString(),
                    password = UUID.randomUUID().toString(),
                    recipients = listOf(),
                )

            sut.handle(ticketOpens)
        }
    }

    @Nested
    inner class IT {
        @Disabled("Manual test")
        @Test
        fun handle() {
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

            sut.handle(ticketOpens)

            // check if sent to your mail
        }
    }
}
