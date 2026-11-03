package acktsap.adapters.outgoing.interpark

import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import acktsap.fp.Either
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale

private val dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd(EEE) HH:mm", Locale.KOREAN)

fun parseTicketOpen(
    name: String,
    rawTicketOpenTime: String,
    today: LocalDate,
): TicketOpen? {
    val normalizedDate =
        when {
            rawTicketOpenTime.startsWith("오늘 ") -> today.formatDate(rawTicketOpenTime.removePrefix("오늘 "))
            rawTicketOpenTime.startsWith("내일 ") -> today.plusDays(1).formatDate(rawTicketOpenTime.removePrefix("내일 "))
            else -> "${today.year}.$rawTicketOpenTime"
        }

    return Either.catchOrThrow<DateTimeParseException, TicketOpen> {
        TicketOpen(
            name = name,
            platform = Platform.INTERPARK,
            dateTime = LocalDateTime.parse(normalizedDate, dateFormat),
        )
    }.fold({ null }, { it })
}

private fun LocalDate.formatDate(time: String): String =
    "${year.toString().padStart(4, '0')}.${monthValue.toString().padStart(2, '0')}." +
        "${dayOfMonth.toString().padStart(2, '0')}(${dayOfWeek.toKoreanShortName()}) $time"

private fun DayOfWeek.toKoreanShortName(): String =
    when (this) {
        DayOfWeek.MONDAY -> "월"
        DayOfWeek.TUESDAY -> "화"
        DayOfWeek.WEDNESDAY -> "수"
        DayOfWeek.THURSDAY -> "목"
        DayOfWeek.FRIDAY -> "금"
        DayOfWeek.SATURDAY -> "토"
        DayOfWeek.SUNDAY -> "일"
    }
