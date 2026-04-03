package domain.tarot

import agent.TarotReadingAgent
import common.model.tarot.LLMTarotRead
import common.model.tarot.TarotReading
import database.repository.tarot.TarotReadingsRepository
import kotlinx.serialization.json.Json

class TarotService(
    private val tarotReadingsRepository: TarotReadingsRepository
) {

    /**
     * Call this function to get a new card/s reading.
     *
     * @param cardsQuantity The amount of cards to retrieve (1 or 3)
     * @param question The user question
     * @param themes The selected themes
     * @param emotions List of chosen emotions. Ex. ["seeking clarity"]
     *
     * @return the structured JSON with the card/s reading
     */
    suspend fun retrieveTarotReading(
        userId: String,
        userName: String,
        cardsQuantity: Int,
        question: String,
        themes: List<String>,
        emotions: List<String>
    ): LLMTarotRead {
        // 1) Get the previous readings for the userId
        val previousReadings: List<TarotReading> = emptyList() //tarotReadingsRepository.retrieveTarotReadings(userId)

        // 2) Get the card/s for this session
        val cards: List<TarotCard> = TarotCardHelper.getCards(cardsQuantity)

        // 3) Get the reading answer from the agent
        val read = TarotReadingAgent.readCards(userName, question, cards, emotions, themes, previousReadings)
        val json = Json {
            ignoreUnknownKeys = true
        }

        val parsed = try {
            json.decodeFromString<LLMTarotRead>(sanitizeJson(read))
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse LLM response: $read", e)
        }

        // 4) Save the last read in the memory TODO descomentar
        //tarotReadingsRepository.saveRead(userId, parsed.summary, cards, question)

        return parsed
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