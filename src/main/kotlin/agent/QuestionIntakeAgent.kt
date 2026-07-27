package agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import common.model.tarot.QuestionAnalysis
import kotlinx.serialization.json.Json

/**
 * Pre-read intake analyst. Runs BEFORE the reading is generated and returns a
 * compact [QuestionAnalysis] used to (a) reject junk/joke input before any
 * credit is spent, and (b) steer the reading agent's language variant, tone and
 * framing.
 *
 * It NEVER writes the reading. On any failure it returns [QuestionAnalysis.NEUTRAL]
 * (fail-open) so a flaky analysis call can't block or wrongly reject a real reading.
 */
object QuestionIntakeAgent {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyze(
        question: String,
        selectedTheme: String?,
        isForAnotherPerson: Boolean,
    ): QuestionAnalysis {
        // Nothing meaningful to inspect for an open, question-less reading.
        if (question.isBlank()) return QuestionAnalysis.NEUTRAL

        return try {
            val openAiApiKey = System.getenv("OPENAI_API_KEY")
                ?: error("Missing env var: OPENAI_API_KEY")

            val agent = AIAgent(
                promptExecutor = simpleOpenAIExecutor(openAiApiKey),
                llmModel = OpenAIModels.Chat.GPT4oMini,
                systemPrompt = """
                Role: Intake analyst for a professional Spanish-language tarot service.
                You DO NOT write the reading. You only inspect the querent's input and
                return a compact JSON analysis that another agent will use to adapt its
                answer.

                Judge four things:
                1. languageVariant — the Spanish variant the querent writes in:
                   - "es-ES" for Spain markers (vosotros, "vale", "coger", "tío", "guay").
                   - "es-419" for Latin American markers (ustedes, "vos", "acá", "che", "ahorita", "chévere", "órale").
                   - "neutral" when there is no clear regional signal.
                2. tone — one or two words for the emotional register (e.g. "ansioso",
                   "esperanzado", "dolido", "curioso", "casual", "juguetón").
                3. theme — detectedTheme is the real subject of the question (amor,
                   trabajo, familia, dinero, salud emocional, espiritualidad, general…).
                   themeMatch compares it to SELECTED_THEME when one is provided:
                   "match" if consistent, "mismatch" if the question is clearly about
                   something else, "no-theme" if no theme was selected.
                4. quality — judge as a seasoned reader would:
                   - "valid": a sincere question or concern, even if short or vague.
                   - "low": extremely thin/ambiguous but plausibly sincere.
                   - "trash": NOT a real query — placeholder/"lorem ipsum" text,
                     keyboard mashing, pure nonsense, or an obvious joke/troll with no
                     intent to be read. Be conservative: only "trash" when clearly
                     not a sincere request.

                ## Output Format (STRICT)
                - Return ONLY the JSON object. No preamble, no explanation.
                - The output must start with '{' and end with '}'.

                {
                  "languageVariant": "neutral | es-ES | es-419",
                  "tone": "string",
                  "detectedTheme": "string",
                  "themeMatch": "match | mismatch | no-theme",
                  "quality": "valid | low | trash",
                  "reason": "one short sentence"
                }
            """.trimIndent()
            )

            val block = buildString {
                appendLine("### QUESTION")
                appendLine("\"$question\"")
                appendLine("For another person: $isForAnotherPerson")
                appendLine("### SELECTED_THEME")
                appendLine(selectedTheme?.takeIf { it.isNotBlank() } ?: "(none selected)")
            }

            val raw = agent.run(block)
            json.decodeFromString<QuestionAnalysis>(sanitizeJson(raw))
        } catch (e: Exception) {
            println("QuestionIntakeAgent: analysis failed, defaulting to NEUTRAL: ${e.message}")
            QuestionAnalysis.NEUTRAL
        }
    }

    private fun sanitizeJson(raw: String): String =
        raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
}
