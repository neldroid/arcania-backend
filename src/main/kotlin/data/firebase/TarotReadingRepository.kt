package data.firebase

import com.google.cloud.firestore.Firestore
import common.model.tarot.LLMTarotRead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TarotReadingRepository(firestore: Firestore) : FirestoreRepository<LLMTarotRead>(
    firestore = firestore,
    collectionName = "users",
    clazz = LLMTarotRead::class.java,
) {

    suspend fun addReading(userId: String, readingId: String, reading: LLMTarotRead) = withContext(Dispatchers.IO) {
        subCollection(userId, "readings").document(readingId).set(reading)
    }

    suspend fun getLastReadingSummaries(userId: String, limit: Int = 3): List<String> = withContext(Dispatchers.IO) {
        subCollection(userId, "readings")
            .limit(limit)
            .get().get()
            .documents
            .mapNotNull { it.toObject(LLMTarotRead::class.java).summary }
    }

}