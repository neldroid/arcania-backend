package database.repository.tarot

import common.model.tarot.TarotReading
import domain.tarot.TarotCard

interface TarotReadingsRepository {

    suspend fun retrieveTarotReadings(userId: String): List<TarotReading>
    suspend fun saveRead(userId: String, summary: String, cards: List<TarotCard>, question: String)

}