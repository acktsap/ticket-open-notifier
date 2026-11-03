package acktsap.adapters.outgoing.gmail

import acktsap.configuration.MissingSettings
import acktsap.fp.left
import acktsap.fp.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll

class GmailConfigurationTest : StringSpec({
    "resolveMailSettingsShouldAccumulateMissingAndBlankMailFields" {
        val environments =
            listOf(
                emptyMap(),
                mapOf("EMAIL_SENDER" to " ", "EMAIL_PASSWORD" to " ", "EMAIL_RECIPIENT" to " , "),
            )

        val actual = environments.map(::resolveMailSettings)

        actual shouldBe List(2) { MissingSettings(listOf("email sender", "email password", "email recipients")).left() }
    }

    "resolveMailSettingsShouldReadMailCredentialsAndTrimRecipients" {
        checkAll(Arb.int()) { id ->
            val sender = "sender-$id@example.com"
            val password = "password-$id"
            val first = "first-$id@example.com"
            val second = "second-$id@example.com"
            val environment = mapOf("EMAIL_SENDER" to sender, "EMAIL_PASSWORD" to password, "EMAIL_RECIPIENT" to " $first, $second ")

            val actual = resolveMailSettings(environment)

            actual shouldBe MailSettings(sender, password, listOf(first, second)).right()
        }
    }
})
