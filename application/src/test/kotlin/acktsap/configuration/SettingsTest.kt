package acktsap.configuration

import acktsap.application.NotificationSettings
import acktsap.fp.left
import acktsap.fp.right
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import java.nio.file.Path

class SettingsTest : StringSpec({
    "resolveSettingsShouldUseHighestPriorityValuesWithoutRequiringMailConfiguration" {
        checkAll(Arb.string(1..40)) { token ->
            val path = Path.of("visited-$token")
            val layers =
                listOf(
                    SettingsLayer(visitedFilePath = path),
                    SettingsLayer(includeKeywords = listOf("environment-$token")),
                    SettingsLayer(includeKeywords = listOf("file-$token"), excludeKeywords = listOf("exclude-$token")),
                )

            val actual = resolveSettings(layers)

            actual shouldBe NotificationSettings(setOf("environment-$token"), setOf("exclude-$token"), path).right()
        }
    }

    "resolveSettingsShouldReportTheMissingStoragePath" {
        val actual = resolveSettings(emptyList())

        actual shouldBe MissingSettings(listOf("visited file path")).left()
    }

    "resolveSettingsShouldAllowAnEmptyHigherPriorityKeywordListToClearLowerPriorityValues" {
        val path = Path.of("visited.txt")
        val layers =
            listOf(
                SettingsLayer(includeKeywords = emptyList(), visitedFilePath = path),
                SettingsLayer(includeKeywords = listOf("ignored")),
            )

        val actual = resolveSettings(layers)

        actual shouldBe NotificationSettings(emptySet(), emptySet(), path).right()
    }
})
