package data.firebase

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
        subCollection(userId, "dreams").document(interpretationId).set(interpretation)
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
