package acktsap.adapters.outgoing.gmail

import acktsap.application.port.outgoing.NotificationHandler
import acktsap.fp.IO
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

fun gmailHandler(settings: MailSettings): NotificationHandler =
    { plan ->
        if (plan.selected.isEmpty()) IO.pure(Unit) else sendGmail(settings, renderEmail(plan.selected))
    }

fun sendGmail(
    settings: MailSettings,
    email: EmailContent,
): IO<Unit> =
    createSession(settings).flatMap { session ->
        createMessage(session, settings, email).flatMap(::deliver)
    }

private fun createSession(settings: MailSettings): IO<Session> =
    IO.delay {
        val properties =
            Properties().apply {
                setProperty("mail.smtp.host", "smtp.gmail.com")
                setProperty("mail.smtp.port", "465")
                setProperty("mail.smtp.auth", "true")
                setProperty("mail.smtp.connectiontimeout", "10000")
                setProperty("mail.smtp.timeout", "30000")
                setProperty("mail.smtp.writetimeout", "30000")
                setProperty("mail.smtp.socketFactory.port", "465")
                setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            }
        Session.getInstance(
            properties,
            object : Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(settings.sender, settings.password)
            },
        )
    }

private fun createMessage(
    session: Session,
    settings: MailSettings,
    email: EmailContent,
): IO<MimeMessage> =
    IO.delay {
        MimeMessage(session).apply {
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(settings.recipients.joinToString(",")))
            subject = email.title
            setText(email.body)
        }
    }

private fun deliver(message: MimeMessage): IO<Unit> = IO.delay { Transport.send(message) }
