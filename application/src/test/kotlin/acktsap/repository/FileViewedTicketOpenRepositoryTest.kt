package acktsap.repository

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
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

class FileViewedTicketOpenRepositoryTest {
    private val fixtureMonkey =
        FixtureMonkey
            .builder()
            .plugin(KotlinPlugin())
            .build()

    @TempDir
    private lateinit var temp: Path

    @Test
    fun save_and_exists() {
        // given
        val keepTicketOpenBeforeToday = ThreadLocalRandom.current().nextLong(2, 100)
        val dateTime =
            LocalDateTime
                .now()
                .minusDays(ThreadLocalRandom.current().nextLong(0, keepTicketOpenBeforeToday - 1))
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>().copy(dateTime = dateTime)
        val path = temp.resolve(UUID.randomUUID().toString())
        val sut = FileViewedTicketOpenRepository(path, keepTicketOpenBeforeToday)

        // when
        sut.save(ticketOpen)
        val actual = sut.exists(ticketOpen)

        // then
        actual shouldBe true
    }

    @Test
    fun exists_notSaved_returnFalse() {
        // given
        val keepTicketOpenBeforeToday = ThreadLocalRandom.current().nextLong(2, 100)
        val dateTime =
            LocalDateTime
                .now()
                .minusDays(ThreadLocalRandom.current().nextLong(0, keepTicketOpenBeforeToday - 1))
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>().copy(dateTime = dateTime)
        val path = temp.resolve(UUID.randomUUID().toString())
        val sut = FileViewedTicketOpenRepository(path, keepTicketOpenBeforeToday)

        // when
        val actual = sut.exists(ticketOpen)

        // then
        actual shouldBe false
    }

    @Test
    fun exists_savedAsFile_returnTrue() {
        // given
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

        // when
        val actual = sut.exists(ticketOpen)

        // then
        actual shouldBe true
    }

    @Test
    fun exists_savedAsFileWithObsolete_returnFalse() {
        // given
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

        // when
        val actual = sut.exists(ticketOpen)

        // then
        actual shouldBe false
    }
}
