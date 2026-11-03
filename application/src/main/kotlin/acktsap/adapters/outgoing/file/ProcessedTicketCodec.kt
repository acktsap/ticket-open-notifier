package acktsap.adapters.outgoing.file

import acktsap.domain.Platform
import acktsap.domain.TicketOpen
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateTimeFormat = DateTimeFormatter.ISO_LOCAL_DATE_TIME
private const val DELIMITER = ","

fun serializeTicketOpen(ticketOpen: TicketOpen): String =
    listOf(
        ticketOpen.name
            .replace("\\", "\\\\")
            .replace(DELIMITER, "\\$DELIMITER")
            .replace("\r", "\\r")
            .replace("\n", "\\n"),
        ticketOpen.platform.name,
        dateTimeFormat.format(ticketOpen.dateTime),
    ).joinToString(DELIMITER)

fun deserializeTicketOpen(line: String): TicketOpen {
    val fields = splitRecord(line)
    require(fields.size == 3) { "Invalid processed ticket entry" }
    return TicketOpen(
        name = fields[0],
        platform = Platform.valueOf(fields[1]),
        dateTime = LocalDateTime.parse(fields[2], dateTimeFormat),
    )
}

private data class RecordState(
    val fields: PersistentList<String> = persistentListOf(),
    val field: PersistentList<Char> = persistentListOf(),
    val escaping: Boolean = false,
) {
    fun accept(character: Char): RecordState =
        when {
            escaping ->
                copy(
                    field =
                        field.addAll(
                            when (character) {
                                'r' -> listOf('\r')
                                'n' -> listOf('\n')
                                '\\', DELIMITER.single() -> listOf(character)
                                else -> listOf('\\', character)
                            },
                        ),
                    escaping = false,
                )
            character == '\\' -> copy(escaping = true)
            character == DELIMITER.single() -> copy(fields = fields.add(field.joinToString("")), field = persistentListOf())
            else -> copy(field = field.add(character))
        }

    fun finish(): List<String> = fields.add((if (escaping) field.add('\\') else field).joinToString(""))
}

private fun splitRecord(line: String): List<String> = line.fold(RecordState(), RecordState::accept).finish()
