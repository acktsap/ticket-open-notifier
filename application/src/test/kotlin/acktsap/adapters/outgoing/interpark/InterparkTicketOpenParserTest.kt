package acktsap.adapters.outgoing.interpark

import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.checkAll
import java.time.LocalDate

class InterparkTicketOpenParserTest : StringSpec({
    "parseTicketOpenShouldResolveTodayForEveryDateAndMinute" {
        checkAll(Arb.int(0..36_524), Arb.int(0..1_439)) { dayOffset, minuteOfDay ->
            val today = LocalDate.of(2000, 1, 1).plusDays(dayOffset.toLong())
            val hour = minuteOfDay / 60
            val minute = minuteOfDay % 60
            val time = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

            val actual = parseTicketOpen("공연", "오늘 $time", today)

            actual shouldBe
                TicketOpen(
                    "공연",
                    Platform.INTERPARK,
                    today.atStartOfDay().withHour(hour).withMinute(minute),
                )
        }
    }

    "parseTicketOpenShouldResolveTomorrowAcrossDateBoundaries" {
        checkAll(Arb.int(0..36_524), Arb.int(0..1_439)) { dayOffset, minuteOfDay ->
            val today = LocalDate.of(2000, 1, 1).plusDays(dayOffset.toLong())
            val tomorrow = today.plusDays(1)
            val hour = minuteOfDay / 60
            val minute = minuteOfDay % 60
            val time = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

            val actual = parseTicketOpen("공연", "내일 $time", today)

            actual shouldBe TicketOpen("공연", Platform.INTERPARK, tomorrow.atStartOfDay().withHour(hour).withMinute(minute))
        }
    }

    "parseTicketOpenShouldReturnNullWhenDateTextIsInvalid" {
        parseTicketOpen("공연", "오늘 오후", LocalDate.of(2026, 10, 30)) shouldBe null
    }
})
