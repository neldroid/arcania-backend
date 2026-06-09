package domain.repository

import common.model.tarot.LLMTarotRead

interface TarotReadingRepository {
    suspend fun addReading(
        userId: String,
        readingId: String,
        readingType: String,
        reading: LLMTarotRead,
    )

    suspend fun getLastReadingSummaries(userId: String, limit: Int = 3): List<String>

    suspend fun markFailed(userId: String, readingId: String, reason: String)
}
