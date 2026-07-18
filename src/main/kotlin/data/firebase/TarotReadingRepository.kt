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

    /**
     * @param creditToken The exact token in tarot.readings that covered this reading —
     * either the reading's catalog id (a real purchase), its FREE_<TYPE> flag, or FULL_FREE.
     * Whichever one it is gets consumed here.
     */
    suspend fun addReading(userId: String, readingId: String, creditToken: String, reading: LLMTarotRead) = withContext(Dispatchers.IO) {
        // 1) Add the reading document
        subCollection(userId, "readings").document(readingId).set(reading)
        // 2) Remove exactly one token from the readings array (arrayRemove removes ALL occurrences)
        val userRef = collection.document(userId)
        val currentReadings = userRef.get().get()
            .get("tarot.readings")
            ?.let { @Suppress("UNCHECKED_CAST") (it as? List<String>) }
            ?: emptyList()
        val updatedReadings = currentReadings.toMutableList().also { list ->
            val index = list.indexOf(creditToken)
            if (index != -1) list.removeAt(index)
        }
        userRef.update("tarot.readings", updatedReadings).get()
    }

    suspend fun getLastReadingSummaries(userId: String, limit: Int = 3): List<String> = withContext(Dispatchers.IO) {
        subCollection(userId, "readings")
            .limit(limit)
            .get().get()
            .documents
            .mapNotNull { it.toObject(LLMTarotRead::class.java).summary }
    }

}