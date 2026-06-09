package domain.repository

import common.model.dream.LLMDreamInterpretation

interface DreamInterpretationRepository {
    suspend fun addInterpretation(
        userId: String,
        interpretationId: String,
        interpretation: LLMDreamInterpretation,
    )

    suspend fun getLastInterpretationSummaries(userId: String, limit: Int = 3): List<String>

    suspend fun markFailed(userId: String, interpretationId: String, reason: String)
}
