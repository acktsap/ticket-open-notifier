package acktsap.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.time.LocalDateTime

class TicketOpenRulesTest : StringSpec({
    "selectTicketOpensShouldExcludeProcessedAndExcludedTicketsEvenWhenTheyMatchIncludeRules" {
        checkAll(Arb.int()) { id ->
            val allowed = TicketOpen("include-$id", Platform.INTERPARK, LocalDateTime.MIN)
            val excluded = allowed.copy(name = "include-exclude-$id")
            val processed = allowed.copy(name = "include-seen-$id")
            val unrelated = allowed.copy(name = "other-$id")
            val detected = listOf(unrelated, processed, allowed, excluded)
            val rules = TicketOpenRules(setOf("include-"), setOf("exclude-"))

            val actual = selectTicketOpens(detected, setOf(processed), rules)

            actual shouldBe listOf(allowed)
        }
    }

    "selectTicketOpensShouldKeepEveryUnprocessedTicketInInputOrderWhenIncludeRulesAreEmpty" {
        checkAll(Arb.list(Arb.string(0..40), 0..30)) { names ->
            val detected = names.mapIndexed { index, name -> TicketOpen("$index:$name", Platform.INTERPARK, LocalDateTime.MIN) }
            val processed = detected.filterIndexed { index, _ -> index % 2 == 0 }.toSet()
            val expected = detected.filterIndexed { index, _ -> index % 2 != 0 }

            val actual = selectTicketOpens(detected, processed, TicketOpenRules(emptySet(), emptySet()))

            actual shouldBe expected
        }
    }

    "matchesShouldAcceptAnyIncludeKeywordAndRejectAnyExcludeKeyword" {
        val rules = TicketOpenRules(setOf("musical", "concert"), setOf("cancelled", "sold out"))
        val ticket = TicketOpen("concert", Platform.INTERPARK, LocalDateTime.MIN)

        rules.matches(ticket) shouldBe true
        rules.matches(ticket.copy(name = "musical cancelled")) shouldBe false
        rules.matches(ticket.copy(name = "concert sold out")) shouldBe false
        rules.matches(ticket.copy(name = "unrelated")) shouldBe false
    }

    "selectTicketOpensShouldSnapshotResultsAndRuleInputs" {
        val ticket = TicketOpen("show", Platform.INTERPARK, LocalDateTime.MIN)
        val detected = mutableListOf(ticket)
        val includes = mutableSetOf("show")
        val rules = TicketOpenRules(includes, emptySet())
        val selected = selectTicketOpens(detected, emptySet(), rules)

        detected.clear()
        includes.clear()

        selected shouldBe listOf(ticket)
        rules.includeKeywords shouldBe setOf("show")
    }
})
