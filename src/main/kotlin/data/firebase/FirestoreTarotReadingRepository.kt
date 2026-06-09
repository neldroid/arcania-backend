package data.firebase

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import common.model.tarot.LLMTarotRead
import domain.repository.TarotReadingRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val log = KotlinLogging.logger {}

class FirestoreTarotReadingRepository(
    private val firestore: Firestore,
) : TarotReadingRepository {

    private val users get() = firestore.collection(USERS)

    override suspend fun addReading(
        userId: String,
        readingId: String,
        readingType: String,
        reading: LLMTarotRead,
    ) = withContext(Dispatchers.IO) {
        users.document(userId).collection(READINGS).document(readingId).set(reading)

        val userRef = users.document(userId)
        val current = (userRef.get().get().get("tarot.readings") as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()

        val updated = current.toMutableList().apply {
            val idx = indexOf(readingType)
            if (idx != -1) removeAt(idx)
        }
        userRef.update("tarot.readings", updated).get()
        Unit
    }

    override suspend fun getLastReadingSummaries(userId: String, limit: Int): List<String> =
        withContext(Dispatchers.IO) {
            users.document(userId).collection(READINGS)
                .limit(limit)
                .get().get()
                .documents
                .mapNotNull { it.toObject(LLMTarotRead::class.java).summary }
        }

    override suspend fun markFailed(userId: String, readingId: String, reason: String) =
        withContext(Dispatchers.IO) {
            users.document(userId).collection(READINGS).document(readingId).set(
                mapOf(
                    "status" to "FAILED",
                    "failureReason" to reason,
                    "failedAt" to FieldValue.serverTimestamp(),
                )
            ).get()
            log.warn { "tarot.reading.failed userId=$userId readingId=$readingId reason=$reason" }
            Unit
        }

    companion object {
        private const val USERS = "users"
        private const val READINGS = "readings"
    }
}
