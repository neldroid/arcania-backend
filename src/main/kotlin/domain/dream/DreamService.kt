package domain.dream

import agent.DreamInterpretationAgent
import com.google.cloud.Timestamp
import common.model.dream.LLMDreamInterpretation
import data.firebase.DreamInterpretationRepository
import data.firebase.UserRepository
import kotlinx.serialization.json.Json
import java.util.UUID

class DreamService(
    private val dreamRepository: DreamInterpretationRepository,
    private val userRepository: UserRepository,
) {

    suspend fun retrieveDreamInterpretation(
        interpretationId: UUID,
        userId: String,
        userName: String,
        dreamDescription: String,
        themes: List<String>,
        emotions: List<String>,
    ) {
        val availableReadings = userRepository.findUser(userId)?.dream?.readings?.size ?: 0

        if (availableReadings == 0) {
            throw IllegalStateException("Not enough readings left")
        } else {
            interpretDream(interpretationId, userId, userName, dreamDescription, themes, emotions)
        }
    }

    private suspend fun interpretDream(
        interpretationId: UUID,
        userId: String,
        userName: String,
        dreamDescription: String,
        themes: List<String>,
        emotions: List<String>,
    ) {
        val previousInterpretations = dreamRepository.getLastInterpretationSummaries(userId)

        val raw = DreamInterpretationAgent.interpretDream(
            userName = userName,
            dreamDescription = dreamDescription,
            emotions = emotions,
            themes = themes,
            previousInterpretations = previousInterpretations,
        )

        val parsed = try {
            Json.decodeFromString<LLMDreamInterpretation>(sanitizeJson(raw))
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse LLM response: $raw", e)
        }

        dreamRepository.addInterpretation(
            userId = userId,
            interpretationId = interpretationId.toString(),
            interpretation = parsed.copy(createdAt = Timestamp.now()),
        )
    }

    private fun sanitizeJson(raw: String) = raw
        .trim()
        .removePrefix("```json")
        .removePrefix("```")
        .removeSuffix("```")
        .trim()
}
