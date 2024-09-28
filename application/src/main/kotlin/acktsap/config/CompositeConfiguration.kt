package acktsap.config

class CompositeConfiguration(
    vararg configuration: Configuration,
) : Configuration {
    private val configurations: List<Configuration> = configuration.toList()

    override val targetKeywords: List<String>? by lazy {
        configurations
            .asSequence()
            .map { it.targetKeywords }
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
}
