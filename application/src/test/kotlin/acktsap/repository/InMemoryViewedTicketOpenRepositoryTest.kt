package acktsap.repository

import acktsap.fixtureMonkey
import acktsap.model.TicketOpen
import com.navercorp.fixturemonkey.kotlin.giveMe
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.kotest.inspectors.shouldForAll
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class InMemoryViewedTicketOpenRepositoryTest {
    @Test
    fun existsShouldReturnValueWhenItIsSaved() {
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val sut = InMemoryViewedTicketOpenRepository(10)

        sut.save(ticketOpen)
        val actual = sut.exists(ticketOpen)

        actual shouldBe true
    }

    @Test
    fun existsShouldReturnValueWhenItIsNotSaved() {
        val ticketOpen = fixtureMonkey.giveMeOne<TicketOpen>()
        val sut = InMemoryViewedTicketOpenRepository(10)

        val actual = sut.exists(ticketOpen)

        actual shouldBe false
    }

    @Test
    fun saveShouldRemoveOldestOneWhenItIsInvokedMoreThenCapacity() {
        val ticketOpens = fixtureMonkey.giveMe<TicketOpen>(10)
        val sut = InMemoryViewedTicketOpenRepository(3)

        for (ticketOpen in ticketOpens) {
            sut.save(ticketOpen)
        }

        ticketOpens.slice(0..6).shouldForAll { sut.exists(it) shouldBe false }
        ticketOpens.slice(7..9).shouldForAll { sut.exists(it) shouldBe true }
    }
}
