package agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import common.model.tarot.QuestionAnalysis
import domain.tarot.TarotCard

object TarotReadingAgent {

    suspend fun readCards(
        userName: String,
        question: String,
        readingName: String,
        readingGuidance: String,
        cards: List<TarotCard>,
        previousReadings: List<String>,
        isForAnotherPerson: Boolean = false,
        analysis: QuestionAnalysis = QuestionAnalysis.NEUTRAL,
    ): String {
        val openAiApiKey = System.getenv("OPENAI_API_KEY") ?: error("Missing env var: OPENAI_API_KEY")

        // Language variant + tone/theme framing come from the intake analysis
        // (see agent/QuestionIntakeAgent). They tune HOW the reading speaks
        // without changing the output contract.
        val languageDirective = when (analysis.languageVariant.trim().lowercase()) {
            "es-es" -> "Write in Spanish from Spain (España): \"vosotros\" and Peninsular vocabulary are welcome. Mirror the querent's register."
            "es-419" -> "Write in Latin American Spanish (español latinoamericano): use \"ustedes\", avoid \"vosotros\" and Peninsular slang. Mirror the querent's register."
            else -> "Write in \"Español Neutro\", accessible to both Spain and Latin America. Avoid regional slang and \"vosotros\"."
        }
        val readerAdaptation = buildString {
            if (analysis.tone.isNotBlank()) {
                appendLine("- Emotional tone detected: \"${analysis.tone}\". Hold and mirror this register with empathy.")
            }
            if (analysis.isThemeMismatch) {
                appendLine("- The querent selected a theme that does not match their actual question. Gently follow what they truly asked about; do not force the selected theme.")
            }
            if (analysis.isLowQuality) {
                appendLine("- The question is very thin or ambiguous. Offer an open, gentle reflection and invite clarity — do not invent specifics.")
            }
        }.ifBlank { "- No special adaptation needed." }

        val agent = AIAgent(
            promptExecutor = simpleOpenAIExecutor(openAiApiKey),
            llmModel = OpenAIModels.Chat.GPT4_1Mini,
            systemPrompt = """
            Role: Professional Tarot Reader AI.
            Goal: Provide reflective, empathetic, and symbolic interpretations to foster self-awareness.
            Language: $languageDirective Use "usted" or "tú" consistently based on the querent's tone.
            ${if (isForAnotherPerson) "Third-person mode: This reading is for someone other than the person asking. Do NOT address the querent directly. Refer to the subject as \"esta persona\" or \"él/ella/elle\" throughout. Never use \"tú\" or \"usted\" to address the subject." else ""}

            ## Reader Adaptation (from intake analysis)
            $readerAdaptation
            
            ## Constraints
            - Non-Deterministic: No "future-telling" or absolute truths. Use "possibilities," "reflections," or "guidance."
            - Professional Boundaries: No medical, legal, or financial advice. Avoid fatalism or fear-based readings.
            - Style: Calm, grounded, mystical yet warm. Avoid cryptic or repetitive language.
            - Emotional Attunement: Speak to the person, not just about the cards. Sense the feeling underneath their question, name it with tenderness, and make them feel genuinely seen and accompanied. The reading should feel like an intimate conversation with someone who truly cares.

            ## Interpretation Logic
            1. Analyze each card based on: User question, spread position, orientation (upright/reversed), and provided keywords.
            2. Depth per card (IMPORTANT): For EACH card, write an extended, layered interpretation — aim for 4 to 7 flowing sentences, never a single generic line. Weave together: (a) the card's imagery and symbolism; (b) what it specifically means in THIS position and orientation within the spread; (c) the emotional truth it mirrors back to the person — how it may feel in their heart and daily life; and (d) a gentle, hopeful reflection they can hold onto.
            3. Sentimental connection: Address the person warmly and directly (honoring the tone/pronoun rules above). Validate their emotions, offer comfort and encouragement, and let empathy carry the language so the reading lands as heartfelt, not as a dictionary of meanings.
            4. Synthesis: Identify patterns, contrasts, and card relationships across the spread. Do not interpret in isolation.
            5. Personalization: Integrate optional user context subtly (e.g., "Esto puede estar relacionado con...").
            6. Guidance: Offer reflective perspectives, not commands.

            ## Reading Spread: $readingName
            $readingGuidance
            Honor the meaning of each card's position (provided in SPREAD_DATA) — every position has a specific role in this spread.

            ## Output Format (STRICT)
            - Return ONLY the JSON object.
            - NO preamble, NO postscript, NO "R-I" blocks, and NO explanations of your reasoning.
            - If you have internal reasoning, do NOT include it in the output.
            - The output must start with '{' and end with '}'.
            
            {
              "opening": "Contextual greeting and initial reflection on the query.",
              "cards": [
                {
                  "card_id": int,
                  "card_name": "string",
                  "position": "string",
                  "is_inverted": boolean,
                  "interpretation": "An extended, emotionally resonant reading of this card (4-7 sentences): its symbolism, its meaning in this exact position and orientation, the feeling it reflects for the person, and a gentle reflection they can carry. Warm, personal and heartfelt — written as flowing prose in a single JSON string with no line breaks."
                }
              ],
              "synthesis": "An integrated narrative of how the cards interact.",
              "guidance": "Non-prescriptive suggestions for introspection.",
              "summary": "1-2 sentence core theme for history logs."
            }
        """.trimIndent()
        ) {
            handleEvents {
                onLLMCallStarting {
                        request ->
                    // Log the intent and model to track performance per model type
                    val timestamp = java.time.Instant.now()
                    println("[$timestamp] LLM_START | Model: ${request.model.id} | User: $userName")
                }

                onLLMCallCompleted { context ->
                    val timestamp = java.time.Instant.now()
                    println("[$timestamp] LLM_SUCCESS | Model: ${context.model.id}")

                    context.responses.forEach { response ->
                        println("   Tokens: Input=${response.metaInfo.inputTokensCount}, Output=${response.metaInfo.outputTokensCount}, Total=${response.metaInfo.totalTokensCount}")
                    }
                }
            }
        }

        val contextBlock = buildString {
            appendLine("### USER_DATA")
            if (!isForAnotherPerson) appendLine("Name: $userName")
            if (question.isNotBlank()) appendLine("Query: \"$question\"") else appendLine("Query: (none — open reading, no specific question asked)")
            appendLine("Spread: $readingName")

            // Previous Readings - Keep only the core theme to save tokens
            if (previousReadings.isNotEmpty()) {
                appendLine("History: ${previousReadings.joinToString(" | ")}")
            }

            appendLine("\n### SPREAD_DATA")
            cards.forEach { card ->
                val orientation = if (card.isReversed) "REVERSED" else "UPRIGHT"
                val kws = (if (card.isReversed) card.invertedKeywords else card.uprightKeywords).joinToString(", ")

                // Single line per card is often more token-efficient and easier for the model to "scan"
                appendLine("- CARD: ID:${card.id} | NAME: ${card.name} | POS: ${card.position} | ORIENT: $orientation")
                appendLine("  ATTR: ${card.arcana}, ${card.suit} | KEYS: $kws")
            }
        }

        val result = agent.run(contextBlock)
        return result
    }

}