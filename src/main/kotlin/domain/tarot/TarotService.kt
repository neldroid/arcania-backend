package domain.tarot

import agent.TarotReadingAgent
import common.model.tarot.LLMTarotRead
import data.firebase.TarotReadingRepository
import kotlinx.serialization.json.Json
import java.util.UUID

class TarotService(
    private val tarotReadingRepository: TarotReadingRepository
) {

    /**
     * Call this function to get a new card/s reading.
     *
     * @param cardsQuantity The amount of cards to retrieve (1 or 3)
     * @param question The user question
     * @param themes The selected themes
     * @param emotions List of chosen emotions. Ex. ["seeking clarity"]
     *
     * @return the created reading ID
     */
    suspend fun retrieveTarotReading(
        userId: String,
        userName: String,
        cardsQuantity: Int,
        question: String,
        themes: List<String>,
        emotions: List<String>
    ): String {
        // 1) Get the previous readings for the userId
        val previousReadings: List<String> = tarotReadingRepository.getLastReadingSummaries(userId)

        println("PREVIOUS READINGS: $previousReadings")

        // 2) Get the card/s for this session
        val cards: List<TarotCard> = TarotCardHelper.getCards(cardsQuantity)

        // 3) Get the reading answer from the agent
        val read = TarotReadingAgent.readCards(userName, question, cards, emotions, themes, previousReadings)

        val parsed = try {
            Json.decodeFromString<LLMTarotRead>(sanitizeJson(read))
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse LLM response: $read", e)
        }

        val readingId = UUID.randomUUID()

        // 4) Save the result in firebase
        tarotReadingRepository.addReading(
            userId = userId,
            readingId = readingId.toString(),
            reading = parsed
        )

        return readingId.toString()
    }

    private fun sanitizeJson(rawResponse: String): String {
        return rawResponse
            .trim()
            // Removes markdown blocks if they exist
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}