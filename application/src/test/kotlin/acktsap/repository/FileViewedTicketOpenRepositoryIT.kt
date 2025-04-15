package acktsap.repository

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.set
import io.kotest.matchers.shouldBe
import net.jqwik.api.Arbitraries
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.time.LocalDateTime
import java.util.UUID
import java.util.concurrent.ThreadLocalRandom

class FileViewedTicketOpenRepositoryIT {
    @TempDir
    private lateinit var temp: Path

    @Test
    fun existsShouldReturnValueWhenItIsSaved() {
        val keepTicketOpenBeforeToday = ThreadLocalRandom.current().nextLong(2, 100)
        val dateTime =
            LocalDateTime
                .now()
                .minusDays(ThreadLocalRandom.current().nextLong(0, keepTicketOpenBeforeToday - 1))
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>().copy(dateTime = dateTime)
        val path = temp.resolve(UUID.randomUUID().toString())
        val sut = FileViewedTicketOpenRepository(path, keepTicketOpenBeforeToday)

        sut.save(ticketOpen)
        val actual = sut.exists(ticketOpen)

        actual shouldBe true
    }

    @Test
    fun existsShouldReturnValueWhenItIsNotSaved() {
        val keepTicketOpenBeforeToday = ThreadLocalRandom.current().nextLong(2, 100)
        val dateTime =
            LocalDateTime
                .now()
                .minusDays(ThreadLocalRandom.current().nextLong(0, keepTicketOpenBeforeToday - 1))
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>().copy(dateTime = dateTime)
        val path = temp.resolve(UUID.randomUUID().toString())
        val sut = FileViewedTicketOpenRepository(path, keepTicketOpenBeforeToday)

        val actual = sut.exists(ticketOpen)

        actual shouldBe false
    }

    @Test
    fun existsShouldReturnValueWhenItIsSavedAsFileAndReloaded() {
        val keepTicketOpenBeforeToday = ThreadLocalRandom.current().nextLong(1, 100)
        val dateTime =
            LocalDateTime
                .now()
                .minusDays(ThreadLocalRandom.current().nextLong(0, keepTicketOpenBeforeToday))
        val ticketOpen =
            fixtureMonkey
                .giveMeBuilder<TicketOpen>()
                .set(TicketOpen::name, Arbitraries.strings().alpha())
                .set(TicketOpen::dateTime, dateTime)
                .sample()
        val path = temp.resolve(UUID.randomUUID().toString())
        FileViewedTicketOpenRepository(path, keepTicketOpenBeforeToday).use {
            it.save(ticketOpen)
        }
        val sut = FileViewedTicketOpenRepository(path, keepTicketOpenBeforeToday)

        val actual = sut.exists(ticketOpen)

        actual shouldBe true
    }

    @Test
    fun existsShouldNotReturnValueWhenItIsSavedAsFileButItIsObsoleteAndReloaded() {
        val savedDayBefore = ThreadLocalRandom.current().nextLong(1, 100)
        val dateTime =
            LocalDateTime
                .now()
                .minusDays(savedDayBefore)
        val ticketOpen =
            fixtureMonkey
                .giveMeBuilder<TicketOpen>()
                .set(TicketOpen::name, Arbitraries.strings().alpha())
                .set(TicketOpen::dateTime, dateTime)
                .sample()
        val path = temp.resolve(UUID.randomUUID().toString())
        FileViewedTicketOpenRepository(path, savedDayBefore - 1).use {
            it.save(ticketOpen)
        }
        val sut = FileViewedTicketOpenRepository(path, ThreadLocalRandom.current().nextLong())

        val actual = sut.exists(ticketOpen)

        actual shouldBe false
    }
}
