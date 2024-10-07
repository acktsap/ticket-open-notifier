package acktsap.repository

import acktsap.model.Platform
import acktsap.model.TicketOpen
import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.io.path.bufferedReader
import kotlin.io.path.bufferedWriter
import kotlin.io.path.exists

private val logger = KotlinLogging.logger { }

/**
 * File format
 *
 * each line : name:platform:dateTime
 */
class FileViewedTicketOpenRepository(
    private val path: Path,
    private val keepTicketOpenBeforeToday: Long,
) : ViewedTicketOpenRepository {
    private val openedSet: MutableSet<TicketOpen>

    init {
        val openedSet = mutableSetOf<TicketOpen>()
        if (path.exists()) {
            path.bufferedReader().use {
                it.forEachLine { line ->
                    logger.trace { "Reading '$line'" }
                    openedSet.add(deserialize(line))
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
        path.bufferedWriter().use {
            val baseDateTime = LocalDateTime.now().minusDays(keepTicketOpenBeforeToday)
            val filtered = openedSet.filter { ticketOpen -> ticketOpen.dateTime.isAfter(baseDateTime) }
            logger.info { "${openedSet.size - filtered.size} items removed as obsolete (baseDateTime: $baseDateTime)" }
            logger.info { "Saving ${filtered.size} items to '$path' (${openedSet.size - filtered.size} items removed as obsolete)" }
            for (ticketOpen in filtered) {
                val line = serialize(ticketOpen)
                logger.trace { "Saving '$line'" }
                it.write("$line\n")
            }
        }
    }

    private fun deserialize(line: String): TicketOpen {
        val (name, rawPlatform, rawDateTime) = line.split(DELIMITER)
        return TicketOpen(
            name = name,
            platform = Platform.valueOf(rawPlatform),
            dateTime = LocalDateTime.parse(rawDateTime, DATETIME_FORMATER),
        )
    }

    private fun serialize(ticketOpen: TicketOpen): String {
        val name = ticketOpen.name
        val platform = ticketOpen.platform
        val dateTime = DATETIME_FORMATER.format(ticketOpen.dateTime)
        return "$name$DELIMITER$platform$DELIMITER$dateTime"
    }

    companion object {
        private val DATETIME_FORMATER = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        private const val DELIMITER = ","
    }
}
