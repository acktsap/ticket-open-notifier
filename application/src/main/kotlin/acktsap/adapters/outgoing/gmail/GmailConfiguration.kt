package acktsap.adapters.outgoing.gmail

import acktsap.application.port.outgoing.NotificationHandler
import acktsap.configuration.MissingSettings
import acktsap.fp.Either
import acktsap.fp.IO
import acktsap.fp.Validated
import acktsap.fp.asValidated
import acktsap.fp.instances.ValidatedApplicative
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList

data class MailSettings(
    val sender: String,
    val password: String,
    val recipients: PersistentList<String>,
) {
    constructor(sender: String, password: String, recipients: List<String>) :
        this(sender, password, recipients.toPersistentList())
}

fun systemGmailHandler(): IO<Either<MissingSettings, NotificationHandler>> =
    IO.delay { resolveMailSettings(System.getenv()) }.map { result -> result.map(::gmailHandler) }

fun resolveMailSettings(environment: Map<String, String>): Either<MissingSettings, MailSettings> {
    val sender = environment["EMAIL_SENDER"]?.takeIf(String::isNotBlank)
    val password = environment["EMAIL_PASSWORD"]?.takeIf(String::isNotBlank)
    val recipients = environment["EMAIL_RECIPIENT"]?.split(',')?.map(String::trim)?.filter(String::isNotEmpty)?.takeIf { it.isNotEmpty() }
    val validation = ValidatedApplicative<String>()
    val credentials =
        validation.map2(
            Validated.required(sender, "email sender"),
            Validated.required(password, "email password"),
        ) { validSender, validPassword -> validSender to validPassword }
    return validation.map2(
        credentials,
        Validated.required(recipients, "email recipients"),
    ) { (validSender, validPassword), validRecipients ->
        MailSettings(validSender, validPassword, validRecipients)
    }.asValidated().toEither().mapLeft(::MissingSettings)
}
