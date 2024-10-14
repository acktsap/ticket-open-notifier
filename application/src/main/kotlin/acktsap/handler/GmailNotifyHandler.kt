package acktsap.handler

import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

private val logger = KotlinLogging.logger { }

class GmailNotifyHandler(
    private val username: String,
    private val password: String,
    private val recipients: List<String>,
) : TicketOpenHandler {
    override fun handle(ticketOpens: Collection<TicketOpen>) {
        if (ticketOpens.isEmpty()) {
            logger.info { "0 ticketOpens found. No gmail notification" }
            return
        }

        val title = buildTitle(ticketOpens)
        val content = buildContent(ticketOpens)
        sendEmail(title, content)
        logger.info { "Success to make email notification" }
    }

    private fun buildTitle(ticketOpens: Collection<TicketOpen>): String {
        return "티켓 오픈 알람 (${ticketOpens.size}개)"
    }

    internal fun buildContent(ticketOpens: Collection<TicketOpen>): String {
        return ticketOpens
            .sortedBy { it.dateTime }
            .joinToString(separator = "\n\n") {
                val dateTime = "${it.dateTime.toLocalDate()} ${it.dateTime.toLocalTime()}"
                "$dateTime\n${it.name} ${it.platform}"
            }
    }

    private fun sendEmail(
        title: String,
        content: String,
    ) {
        val prop =
            Properties().apply {
                set("mail.smtp.host", "smtp.gmail.com")
                set("mail.smtp.port", "465")
                set("mail.smtp.auth", "true")
                set("mail.smtp.socketFactory.port", "465")
                set("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            }
        val session =
            Session.getInstance(
                prop,
                object : Authenticator() {
                    override fun getPasswordAuthentication(): PasswordAuthentication = PasswordAuthentication(username, password)
                },
            )

        val joinedRecipients = recipients.joinToString(",")
        val recipientAddresses = InternetAddress.parse(joinedRecipients)
        val message =
            MimeMessage(session).apply {
                setRecipients(
                    Message.RecipientType.TO,
                    recipientAddresses,
                )
                subject = title
                setText(content)
            }
        Transport.send(message)
    }
}
