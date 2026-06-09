package common.config

/**
 * Centralized, validated runtime configuration.
 *
 * Every `System.getenv` call lives here. Missing required values fail
 * at startup with a precise message, not at the first call site that
 * needs them. Injected via Koin; consumers never read env vars directly.
 */
data class AppConfig(
    val geminiApiKey: String,
    val firebaseServiceAccount: String,
    val internalApiKey: String,
    val stripeWebhookSecret: String,
    val makeWebhookSecret: String,
    val internalBaseUrl: String,
    val llmTimeoutSeconds: Long,
) {
    companion object {
        fun fromEnv(env: (String) -> String? = System::getenv): AppConfig {
            fun require(name: String): String =
                env(name) ?: error("Missing required environment variable: $name")

            return AppConfig(
                geminiApiKey = require("GEMINI_API_KEY"),
                firebaseServiceAccount = require("FIREBASE_SERVICE_ACCOUNT"),
                internalApiKey = require("TAROT_API_KEY"),
                stripeWebhookSecret = require("STRIPE_WEBHOOK_SECRET"),
                makeWebhookSecret = require("MAKE_WEBHOOK_SECRET"),
                internalBaseUrl = env("INTERNAL_BASE_URL") ?: "http://localhost:8080",
                llmTimeoutSeconds = env("LLM_TIMEOUT_SECONDS")?.toLongOrNull() ?: 90L,
            )
        }
    }
}
