package acktsap.config

import java.nio.file.Path

class CompositeConfiguration(
    vararg configuration: Configuration,
) : Configuration {
    private val configurations: List<Configuration> = configuration.toList()

    override val includeKeywords: List<String>? by lazy {
        configurations
            .asSequence()
            .map { it.includeKeywords }
            .firstOrNull { it != null }
    }

    override val excludeKeywords: List<String>? by lazy {
        configurations
            .asSequence()
            .map { it.excludeKeywords }
            .firstOrNull { it != null }
    }

    override val emailSender: String? by lazy {
        configurations
            .asSequence()
            .map { it.emailSender }
            .firstOrNull { it != null }
    }

    override val emailSenderPassword: String? by lazy {
        configurations
            .asSequence()
            .map { it.emailSenderPassword }
            .firstOrNull { it != null }
    }

    override val emailRecipients: List<String>? by lazy {
        configurations
            .asSequence()
            .map { it.emailRecipients }
            .firstOrNull { it != null }
    }

    override val visitedFilePath: Path? by lazy {
        configurations
            .asSequence()
            .map { it.visitedFilePath }
            .firstOrNull { it != null }
    }

    override fun merge(other: Configuration): Configuration {
        val configurations = this.configurations + other
        return CompositeConfiguration(
            *configurations.toTypedArray(),
        )
    }
}
