package common.model.tarot

import kotlinx.serialization.Serializable

/**
 * Structured pre-read analysis produced by [agent.QuestionIntakeAgent] and
 * injected into the tarot reading agent to steer language, tone and framing.
 *
 * All fields are lenient strings so a slightly-off LLM value never breaks
 * deserialization; meaning is derived through the helpers below.
 */
@Serializable
data class QuestionAnalysis(
    /** neutral | es-ES | es-419 — the Spanish variant the querent writes in. */
    val languageVariant: String = "neutral",
    /** Short emotional register, e.g. "ansioso", "esperanzado", "casual". */
    val tone: String = "",
    /** Theme inferred from the question itself, e.g. "amor", "trabajo". */
    val detectedTheme: String = "",
    /** match | mismatch | no-theme — vs the theme the user selected, if any. */
    val themeMatch: String = "no-theme",
    /** valid | low | trash */
    val quality: String = "valid",
    /** One-line rationale, mainly for low/trash/mismatch. */
    val reason: String = "",
) {
    val isTrash: Boolean get() = quality.trim().equals("trash", ignoreCase = true)
    val isLowQuality: Boolean get() = quality.trim().equals("low", ignoreCase = true)
    val isThemeMismatch: Boolean get() = themeMatch.trim().equals("mismatch", ignoreCase = true)

    companion object {
        /**
         * Fail-open default: a valid, neutral question. Used when intake is
         * skipped (no question to inspect) or the analysis fails, so a flaky
         * intake call never blocks or wrongly rejects a legitimate reading.
         */
        val NEUTRAL = QuestionAnalysis()
    }
}
