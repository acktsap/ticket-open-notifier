package acktsap.config

import java.nio.file.Path

class InMemoryConfiguration(
    override val includeKeywords: List<String>? = null,
    override val emailSender: String? = null,
    override val emailSenderPassword: String? = null,
    override val emailRecipients: List<String>? = null,
    override val visitedFilePath: Path? = null,
) : Configuration {
    override fun merge(other: Configuration): Configuration {
        val configurations = listOf(this, other)
        return CompositeConfiguration(
            *configurations.toTypedArray(),
        )
    }
}
