package acktsap.adapters.outgoing.file

import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.time.LocalDateTime
import java.time.ZoneOffset

class ProcessedTicketCodecTest : StringSpec({
    "serializeTicketOpenShouldRoundTripArbitraryPrintableNamesAndTimes" {
        checkAll(Arb.string(0..80), Arb.long(-10_000_000L..10_000_000L), Arb.int(0..999_999_999)) { name, epochSecond, nano ->
            val ticket =
                TicketOpen(
                    name,
                    Platform.INTERPARK,
                    LocalDateTime.ofEpochSecond(epochSecond, nano, ZoneOffset.UTC),
                )

            val actual = deserializeTicketOpen(serializeTicketOpen(ticket))

            actual shouldBe ticket
        }
    }
    "serializeTicketOpenShouldRoundTripNamesBuiltFromEscapesAndDelimiters" {
        checkAll(Arb.list(Arb.element('\\', ',', '\r', '\n', 'x', '한'), 0..200)) { characters ->
            val ticket = TicketOpen(characters.joinToString(""), Platform.INTERPARK, LocalDateTime.of(2026, 10, 30, 12, 0))

            deserializeTicketOpen(serializeTicketOpen(ticket)) shouldBe ticket
        }
    }

    "deserializeTicketOpenShouldPreserveUnknownLegacyEscapes" {
        val ticket = deserializeTicketOpen("legacy\\x,INTERPARK,2026-10-30T12:00:00")

        ticket.name shouldBe "legacy\\x"
    }
})
