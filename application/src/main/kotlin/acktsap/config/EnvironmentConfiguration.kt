package acktsap.config

class EnvironmentConfiguration : Configuration {
    override val targetKeywords: List<String>? by lazy { getenv(KEYWORDS)?.split(",") }
    override val emailSender: String? by lazy { getenv(EMAIL_SENDER) }
    override val emailSenderPassword: String? by lazy { getenv(EMAIL_PASSWORD) }
    override val emailRecipients: List<String>? by lazy { getenv(EMAIL_RECIPIENTS)?.split(",") }

    companion object {
        fun getenv(key: String): String? = System.getenv(key)

        private const val KEYWORDS = "KEYWORD"
        private const val EMAIL_SENDER = "EMAIL_SENDER"
        private const val EMAIL_PASSWORD = "EMAIL_PASSWORD"
        private const val EMAIL_RECIPIENTS = "EMAIL_RECIPIENT"
    }
}
