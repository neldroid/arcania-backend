package agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import domain.tarot.TarotCard

object TarotReadingAgent {

    suspend fun readCards(
        userName: String,
        question: String,
        cards: List<TarotCard>,
        emotions: List<String>,
        themes: List<String>,
        previousReadings: List<String>,
        isForAnotherPerson: Boolean = false,
    ): String {
        val geminiApiKey = System.getenv("GEMINI_API_KEY") ?: error("Missing env var: GEMINI_API_KEY")

        val model = GoogleModels.Gemini2_5Flash

        val agent = AIAgent(
            promptExecutor = simpleGoogleAIExecutor(geminiApiKey),
            llmModel = model,
            systemPrompt = """
            Role: Professional Tarot Reader AI.
            Goal: Provide reflective, empathetic, and symbolic interpretations to foster self-awareness.
            Language: Output must be in "Español Neutro" (accessible to both Spain and Latin America). Avoid regional slang (e.g., avoid "vosotros" or heavy "lunfardo"). Use "usted" or "tú" consistently based on the user's tone.
            ${if (isForAnotherPerson) "Third-person mode: This reading is for someone other than the person asking. Do NOT address the querent directly. Refer to the subject as \"esta persona\" or \"él/ella/elle\" throughout. Never use \"tú\" or \"usted\" to address the subject." else ""}
            
            ## Constraints
            - Non-Deterministic: No "future-telling" or absolute truths. Use "possibilities," "reflections," or "guidance."
            - Professional Boundaries: No medical, legal, or financial advice. Avoid fatalism or fear-based readings.
            - Style: Calm, grounded, mystical yet clear. Avoid cryptic or repetitive language.
            
            ## Interpretation Logic
            1. Analyze each card based on: User question, spread position, orientation (upright/reversed), and provided keywords.
            2. Synthesis: Identify patterns, contrasts, and card relationships. Do not interpret in isolation.
            3. Personalization: Integrate optional user context subtly (e.g., "This may relate to...").
            4. Guidance: Offer reflective perspectives, not commands.
            
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
                  "interpretation": "Concise analysis tied to the question and position."
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
            appendLine("Query: \"$question\"")

            // Condensed Context
            if (themes.isNotEmpty() || emotions.isNotEmpty()) {
                val themeStr = themes.joinToString().takeIf { it.isNotEmpty() } ?: "None"
                val emotionStr = emotions.joinToString().takeIf { it.isNotEmpty() } ?: "None"
                appendLine("Context: [Themes: $themeStr | Emotions: $emotionStr]")
            }

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