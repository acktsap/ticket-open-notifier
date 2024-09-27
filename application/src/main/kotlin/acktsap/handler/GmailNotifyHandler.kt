package acktsap.handler

import acktsap.model.TicketOpen
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

class GmailNotifyHandler(
    private val username: String,
    private val password: String,
    private val recipients: List<String>,
) : TicketOpenHandler {
    override fun handle(ticketOpens: Collection<TicketOpen>) {
        val title = "티켓 오픈 알람 (${ticketOpens.size}개)"
        val content =
            ticketOpens
                .sortedBy { it.dateTime }
                .joinToString(separator = "\n") {
                    "${it.dateTime} ${it.name} ${it.platform}"
                }
        sendEmail(title, content)
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
