package acktsap.config

class EnvironmentConfiguration : Configuration {
    override val targetKeywords: List<String>? = getenv(KEYWORDS)?.split(",")
    override val emailSender: String? = getenv(EMAIL_SENDER)
    override val emailSenderPassword: String? = getenv(EMAIL_PASSWORD)
    override val emailRecipients: List<String>? = getenv(EMAIL_RECIPIENTS)?.split(",")

    companion object {
        fun getenv(key: String): String? = System.getenv(key)

        private const val KEYWORDS = "KEYWORD"
        private const val EMAIL_SENDER = "EMAIL_SENDER"
        private const val EMAIL_PASSWORD = "EMAIL_PASSWORD"
        private const val EMAIL_RECIPIENTS = "EMAIL_RECIPIENT"
    }
}
