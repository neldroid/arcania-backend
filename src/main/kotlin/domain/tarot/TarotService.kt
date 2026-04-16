package domain.tarot

import agent.TarotReadingAgent
import common.model.tarot.LLMTarotRead
import data.firebase.TarotReadingRepository
import data.firebase.UserRepository
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.json.Json
import java.util.UUID

class TarotService(
    private val tarotReadingRepository: TarotReadingRepository,
    private val userRepository: UserRepository,
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
        readingId: UUID,
        userId: String,
        userName: String,
        cardsQuantity: Int,
        question: String,
        themes: List<String>,
        emotions: List<String>
    ) {
        // 0) Get the available readings for the user
        val availableReadings = userRepository.findUser(userId)?.tarot?.readingsAmount ?: 0

        if (availableReadings == 0) {
            throw IllegalStateException("Not enough readings left")
        } else {
            readTarot(readingId, userId, userName, cardsQuantity, question, themes, emotions)
        }
    }

    private suspend fun readTarot(
        readingId: UUID,
        userId: String,
        userName: String,
        cardsQuantity: Int,
        question: String,
        themes: List<String>,
        emotions: List<String>
    ) {
        // 1) Get the previous readings for the userId
        val previousReadings: List<String> = tarotReadingRepository.getLastReadingSummaries(userId)

        // 2) Get the card/s for this session
        val cards: List<TarotCard> = TarotCardHelper.getCards(cardsQuantity)

        // 3) Get the reading answer from the agent
        val read = TarotReadingAgent.readCards(userName, question, cards, emotions, themes, previousReadings)

        val parsed = try {
            Json.decodeFromString<LLMTarotRead>(sanitizeJson(read))
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse LLM response: $read", e)
        }

        // 4) Add the createdAt date
        val llmTarotRead = parsed.copy(
            createdAt = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        )

        // 5) Save the result in firebase
        tarotReadingRepository.addReading(
            userId = userId,
            readingId = readingId.toString(),
            reading = llmTarotRead,
        )
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