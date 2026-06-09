package agent.prompts

import domain.tarot.TarotCard

/**
 * Prompt definitions for the tarot reading agent.
 *
 * Prompts live here as data so they can be A/B tested, versioned, hot-reloaded,
 * or moved to a remote config store without touching service or provider code.
 */
object TarotPrompts {

    const val VERSION = "v1.0"

    fun systemPrompt(isForAnotherPerson: Boolean): String = """
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

    fun contextBlock(
        userName: String,
        question: String,
        cards: List<TarotCard>,
        themes: List<String>,
        emotions: List<String>,
        previousReadings: List<String>,
        isForAnotherPerson: Boolean,
    ): String = buildString {
        appendLine("### USER_DATA")
        if (!isForAnotherPerson) appendLine("Name: $userName")
        appendLine("Query: \"$question\"")

        if (themes.isNotEmpty() || emotions.isNotEmpty()) {
            val themeStr = themes.joinToString().ifEmpty { "None" }
            val emotionStr = emotions.joinToString().ifEmpty { "None" }
            appendLine("Context: [Themes: $themeStr | Emotions: $emotionStr]")
        }

        if (previousReadings.isNotEmpty()) {
            appendLine("History: ${previousReadings.joinToString(" | ")}")
        }

        appendLine()
        appendLine("### SPREAD_DATA")
        cards.forEach { card ->
            val orientation = if (card.isReversed) "REVERSED" else "UPRIGHT"
            val kws = (if (card.isReversed) card.invertedKeywords else card.uprightKeywords).joinToString(", ")
            appendLine("- CARD: ID:${card.id} | NAME: ${card.name} | POS: ${card.position} | ORIENT: $orientation")
            appendLine("  ATTR: ${card.arcana}, ${card.suit} | KEYS: $kws")
        }
    }
}
