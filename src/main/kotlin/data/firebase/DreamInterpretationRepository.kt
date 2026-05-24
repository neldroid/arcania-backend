package data.firebase

import com.google.cloud.Timestamp
import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import common.model.dream.LLMDreamInterpretation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

class DreamInterpretationRepository(firestore: Firestore) : FirestoreRepository<LLMDreamInterpretation>(
    firestore = firestore,
    collectionName = "users",
    clazz = LLMDreamInterpretation::class.java,
) {

    suspend fun addInterpretation(
        userId: String,
        interpretationId: String,
        interpretation: LLMDreamInterpretation,
    ) = withContext(Dispatchers.IO) {
        subCollection(userId, "dreams").document(interpretationId).set(interpretation)

        update(userId, mapOf(
            "dream.readingsAmount" to FieldValue.increment(-1),
            "dream.nextFreeReadingAt" to nextFreeReadingDate(),
        ))
    }

    suspend fun getLastInterpretationSummaries(userId: String, limit: Int = 3): List<String> =
        withContext(Dispatchers.IO) {
            subCollection(userId, "dreams")
                .limit(limit)
                .get().get()
                .documents
                .mapNotNull { it.toObject(LLMDreamInterpretation::class.java).summary }
        }

}
