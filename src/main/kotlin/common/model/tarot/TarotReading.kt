package common.model.tarot

import kotlinx.serialization.Serializable

/**
 * @param question - The question summary made by the user
 * @param answer - The answer summary get from the AI agent
 * @param themes - Related themes for the question. Ex ["career uncertainty"]
 */
@Serializable
data class TarotReading(
    val userId: String,
    val question: String,
    val answer: String,
    val themes: List<String>,
    val cardIds: List<Int>,
    val createdAt: Long
)
