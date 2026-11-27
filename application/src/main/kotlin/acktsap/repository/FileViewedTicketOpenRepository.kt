package acktsap.repository

import acktsap.model.Platform
import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

private val logger = KotlinLogging.logger { }

/** Stores headerless CSV records in name, platform, dateTime order. */
class FileViewedTicketOpenRepository(
    private val path: Path,
    private val keepTicketOpenBeforeToday: Long,
) : ViewedTicketOpenRepository {
    private val openedSet: MutableSet<TicketOpen>

    init {
        val openedSet = mutableSetOf<TicketOpen>()
        if (path.exists()) {
            CSVFormat.DEFAULT.parse(path.bufferedReader()).use { parser ->
                parser.forEach { record ->
                    logger.info { "Reading '$record'" }
                    openedSet.add(deserialize(record))
                }
            }
            logger.info { "Found ${openedSet.size} items from '$path" }
        }
        this.openedSet = openedSet
    }

    override fun save(ticketOpen: TicketOpen) {
        openedSet.add(ticketOpen)
    }

    override fun exists(ticketOpen: TicketOpen): Boolean = openedSet.contains(ticketOpen)

    override fun close() {
        CSVFormat.DEFAULT.print(path.bufferedWriter()).use { printer ->
            val baseDateTime = LocalDateTime.now().minusDays(keepTicketOpenBeforeToday)
            val filtered = openedSet.filter { ticketOpen -> ticketOpen.dateTime.isAfter(baseDateTime) }
            logger.info { "${openedSet.size - filtered.size} items removed as obsolete (baseDateTime: $baseDateTime)" }
            logger.info { "Saving ${filtered.size} items to '$path' (${openedSet.size - filtered.size} items removed as obsolete)" }
            for (ticketOpen in filtered) {
                logger.trace { "Saving '$ticketOpen'" }
                printer.printRecord(ticketOpen.name, ticketOpen.platform, DATETIME_FORMATER.format(ticketOpen.dateTime))
            }
        }
    }

    private fun deserialize(record: CSVRecord): TicketOpen {
        require(record.size() == 3) { "Expected 3 CSV fields in record ${record.recordNumber}, got ${record.size()}" }
        return TicketOpen(
            name = record[0],
            platform = Platform.valueOf(record[1]),
            dateTime = LocalDateTime.parse(record[2], DATETIME_FORMATER),
        )
    }

    companion object {
        private val DATETIME_FORMATER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    }
}
