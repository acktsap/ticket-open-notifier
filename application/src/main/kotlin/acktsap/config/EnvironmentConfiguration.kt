package acktsap.config

import java.nio.file.Path

class EnvironmentConfiguration : Configuration {
    override val includeKeywords: List<String>? by lazy { getenv(KEYWORDS)?.split(",") }
    override val emailSender: String? by lazy { getenv(EMAIL_SENDER) }
    override val emailSenderPassword: String? by lazy { getenv(EMAIL_PASSWORD) }
    override val emailRecipients: List<String>? by lazy { getenv(EMAIL_RECIPIENTS)?.split(",") }
    override val visitedFilePath: Path? by lazy { getenv(VISITED_FILE_PATH)?.let { Path.of(it) } }

    override fun merge(other: Configuration): Configuration {
        val configurations = listOf(this, other)
        return CompositeConfiguration(
            *configurations.toTypedArray(),
        )
    }

    private fun getenv(key: String): String? = System.getenv(key)

    companion object {
        private const val KEYWORDS = "KEYWORD"
        private const val EMAIL_SENDER = "EMAIL_SENDER"
        private const val EMAIL_PASSWORD = "EMAIL_PASSWORD"
        private const val EMAIL_RECIPIENTS = "EMAIL_RECIPIENT"
        private const val VISITED_FILE_PATH = "VISITED_FILE_PATH"
    }
}
