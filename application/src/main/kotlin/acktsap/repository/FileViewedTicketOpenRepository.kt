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

/**
 * File format
 *
 * each line : name:platform:dateTime
 */
class FileViewedTicketOpenRepository(
    private val path: Path,
) : ViewedTicketOpenRepository {
    private val openedSet: MutableSet<TicketOpen>

    init {
        val openedSet = mutableSetOf<TicketOpen>()
        if (path.exists()) {
            LOGGER.info { "Reading visited information from '$path'" }
            path.bufferedReader().use {
                it.forEachLine { line ->
                    LOGGER.trace { "Reading '$line'" }
                    openedSet.add(deserialize(line))
                }
            }
        }
        this.openedSet = openedSet
    }

    override fun save(ticketOpen: TicketOpen) {
        openedSet.add(ticketOpen)
    }

    override fun exists(ticketOpen: TicketOpen): Boolean = openedSet.contains(ticketOpen)

    override fun close() {
        path.bufferedWriter().use {
            LOGGER.info { "Saving visited information to '$path'" }
            for (ticketOpen in openedSet) {
                val line = serialize(ticketOpen)
                LOGGER.trace { "Saving '$line'" }
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
        private val LOGGER = KotlinLogging.logger { }
        private const val DELIMITER = ","
    }
}
