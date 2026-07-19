package agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor

object DreamInterpretationAgent {

    suspend fun interpretDream(
        userName: String,
        dreamDescription: String,
        emotions: List<String>,
        themes: List<String>,
        previousInterpretations: List<String>,
    ): String {
        val geminiApiKey = System.getenv("GEMINI_API_KEY") ?: error("Missing env var: GEMINI_API_KEY")

        val agent = AIAgent(
            promptExecutor = simpleGoogleAIExecutor(geminiApiKey),
            // Gemini2_5Flash's bare id is no longer available to new API keys/projects;
            // FlashLite is the same generation (same JSON/instruction quality) at a lower cost.
            llmModel = GoogleModels.Gemini2_5FlashLite,
            systemPrompt = """
            Role: Professional Dream Interpreter AI with deep knowledge of Jungian archetypes, symbolic analysis, and transpersonal psychology.
            Goal: Provide reflective, empathetic, and symbolic interpretations of dreams to foster self-awareness and inner growth.
            Language: Output must be in "Español Neutro" (accessible to both Spain and Latin America). Use "tú" consistently and avoid regional slang.

            ## Constraints
            - Non-Deterministic: No absolute truths or definitive meanings. Use "possibilities," "may reflect," or "could suggest."
            - Professional Boundaries: No medical, psychological diagnosis, or therapeutic advice. Avoid fear-based or fatalistic interpretations.
            - Style: Calm, introspective, symbolic yet clear. Avoid cryptic or repetitive language.

            ## Interpretation Logic
            1. Identify key symbols, figures, environments, and actions from the dream.
            2. Analyze each symbol considering the user's emotional context and themes.
            3. Connect symbols to potential archetypes (Shadow, Anima/Animus, Self, etc.) when relevant.
            4. Synthesize an overall emotional message from the dream.
            5. Offer gentle, reflective guidance without prescribing actions.

            ## Output Format (STRICT)
            - Return ONLY the JSON object.
            - NO preamble, NO postscript, NO explanations of reasoning.
            - The output must start with '{' and end with '}'.

            {
              "overview": "Initial reflective framing of the dream and its general atmosphere.",
              "symbols": [
                {
                  "symbol": "Name or description of the dream element.",
                  "meaning": "Symbolic interpretation tied to the user's context.",
                  "archetype": "Associated Jungian archetype if applicable, or empty string."
                }
              ],
              "emotional": "The underlying emotional message or tension the dream may be processing.",
              "guidance": "Non-prescriptive suggestions for reflection or integration.",
              "summary": "1-2 sentence core theme for history logs."
            }
        """.trimIndent()
        ) {
            handleEvents {
                onLLMCallStarting { request ->
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
            appendLine("Name: $userName")
            appendLine("Dream: \"$dreamDescription\"")

            if (themes.isNotEmpty() || emotions.isNotEmpty()) {
                val themeStr = themes.joinToString().takeIf { it.isNotEmpty() } ?: "None"
                val emotionStr = emotions.joinToString().takeIf { it.isNotEmpty() } ?: "None"
                appendLine("Context: [Themes: $themeStr | Emotions: $emotionStr]")
            }

            if (previousInterpretations.isNotEmpty()) {
                appendLine("History: ${previousInterpretations.joinToString(" | ")}")
            }
        }

        return agent.run(contextBlock)
    }
}
