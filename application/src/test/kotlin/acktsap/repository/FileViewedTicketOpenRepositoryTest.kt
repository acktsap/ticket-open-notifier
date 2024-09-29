package acktsap.repository

import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.KotlinPlugin
import com.navercorp.fixturemonkey.kotlin.giveMeBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.set
import net.jqwik.api.Arbitraries
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.UUID

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
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val path = temp.resolve(UUID.randomUUID().toString())
        val sut = FileViewedTicketOpenRepository(path)

        // when
        sut.save(ticketOpen)
        val actual = sut.exists(ticketOpen)

        // then
        assertThat(actual).isTrue()
    }

    @Test
    fun exists_notSaved_returnFalse() {
        // given
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val path = temp.resolve(UUID.randomUUID().toString())
        val sut = FileViewedTicketOpenRepository(path)

        // when
        val actual = sut.exists(ticketOpen)

        // then
        assertThat(actual).isFalse()
    }

    @Test
    fun exists_savedAsFile_returnTrue() {
        // given
        val ticketOpen =
            fixtureMonkey
                .giveMeBuilder<TicketOpen>()
                .set(TicketOpen::name, Arbitraries.strings().alpha())
                .sample()
        val path = temp.resolve(UUID.randomUUID().toString())
        FileViewedTicketOpenRepository(path).use {
            it.save(ticketOpen)
        }
        val sut = FileViewedTicketOpenRepository(path)

        // when
        val actual = sut.exists(ticketOpen)

        // then
        assertThat(actual).isTrue()
    }
}
