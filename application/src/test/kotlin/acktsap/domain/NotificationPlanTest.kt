package acktsap.domain

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class NotificationPlanTest : StringSpec({
    "notificationPlanShouldSnapshotBothInputs" {
        val ticket = TicketOpen("show", Platform.INTERPARK, LocalDateTime.MIN)
        val detected = mutableListOf(ticket)
        val selected = mutableListOf(ticket)
        val plan = NotificationPlan(detected, selected)

        detected.clear()
        selected.clear()

        plan.detected shouldBe listOf(ticket)
        plan.selected shouldBe listOf(ticket)
    }
})
