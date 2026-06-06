package data.firebase

import com.google.cloud.Timestamp
import com.google.cloud.firestore.Firestore
import common.model.dream.LLMDreamInterpretation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
        // 1) Save interpretation document
        subCollection(userId, "dreams").document(interpretationId).set(interpretation)
        // 2) Remove exactly one "dream" token (arrayRemove removes ALL occurrences)
        val userRef = collection.document(userId)
        val currentReadings = userRef.get().get()
            .get("dream.readings")
            ?.let { @Suppress("UNCHECKED_CAST") (it as? List<String>) }
            ?: emptyList()
        val updatedReadings = currentReadings.toMutableList().also { list ->
            val index = list.indexOf("dream")
            if (index != -1) list.removeAt(index)
        }
        userRef.update("dream.readings", updatedReadings).get()
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
