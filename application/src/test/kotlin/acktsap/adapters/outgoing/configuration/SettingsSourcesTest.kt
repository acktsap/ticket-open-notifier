package acktsap.adapters.outgoing.configuration

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class SettingsSourcesTest : StringSpec({
    "settingsLayerFromEnvironmentShouldSplitAndTrimCommaSeparatedValues" {
        settingsLayerFromEnvironment(mapOf("INCLUDE_KEYWORDS" to " musical, concert ")).includeKeywords shouldBe
            listOf("musical", "concert")
    }
})
