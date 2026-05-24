package data.firebase

import com.google.cloud.Timestamp
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import common.model.tarot.LLMTarotRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class TarotReadingRepository(firestore: Firestore) : FirestoreRepository<LLMTarotRead>(
    firestore = firestore,
    collectionName = "users",
    clazz = LLMTarotRead::class.java,
) {

    suspend fun addReading(userId: String, readingId: String, reading: LLMTarotRead) = withContext(Dispatchers.IO) {
        // 1) Add the reading
        subCollection(userId, "readings").document(readingId).set(reading)
        // 2) Discount a read
        update(userId, mapOf(
            "tarot.readingsAmount" to FieldValue.increment(-1),
            "tarot.nextFreeReadingAt" to nextFreeReadingDate(),
        ))
    }

    suspend fun getLastReadingSummaries(userId: String, limit: Int = 3): List<String> = withContext(Dispatchers.IO) {
        subCollection(userId, "readings")
            .limit(limit)
            .get().get()
            .documents
            .mapNotNull { it.toObject(LLMTarotRead::class.java).summary }
    }

}