package database.repository.tarot

import common.model.tarot.TarotReading
import domain.tarot.TarotCard

class TarotReadingsRepositoryImpl(): TarotReadingsRepository {

    override suspend fun retrieveTarotReadings(userId: String): List<TarotReading> {
        TODO("Not yet implemented")
    }

    override suspend fun saveRead(
        userId: String,
        summary: String,
        cards: List<TarotCard>,
        question: String
    ) {
        TODO("Not yet implemented")
    }
}