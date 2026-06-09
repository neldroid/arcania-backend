package data.firebase

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import common.model.dream.LLMDreamInterpretation
import domain.repository.DreamInterpretationRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val log = KotlinLogging.logger {}

class FirestoreDreamInterpretationRepository(
    private val firestore: Firestore,
) : DreamInterpretationRepository {

    private val users get() = firestore.collection(USERS)

    override suspend fun addInterpretation(
        userId: String,
        interpretationId: String,
        interpretation: LLMDreamInterpretation,
    ) = withContext(Dispatchers.IO) {
        users.document(userId).collection(DREAMS).document(interpretationId).set(interpretation)

        val userRef = users.document(userId)
        val current = (userRef.get().get().get("dream.readings") as? List<*>)
            ?.filterIsInstance<String>()
            ?: emptyList()
        val updated = current.toMutableList().apply {
            val idx = indexOf("dream")
            if (idx != -1) removeAt(idx)
        }
        userRef.update("dream.readings", updated).get()
        Unit
    }

    override suspend fun getLastInterpretationSummaries(userId: String, limit: Int): List<String> =
        withContext(Dispatchers.IO) {
            users.document(userId).collection(DREAMS)
                .limit(limit)
                .get().get()
                .documents
                .mapNotNull { it.toObject(LLMDreamInterpretation::class.java).summary }
        }

    override suspend fun markFailed(userId: String, interpretationId: String, reason: String) =
        withContext(Dispatchers.IO) {
            users.document(userId).collection(DREAMS).document(interpretationId).set(
                mapOf(
                    "status" to "FAILED",
                    "failureReason" to reason,
                    "failedAt" to FieldValue.serverTimestamp(),
                )
            ).get()
            log.warn { "dream.interpretation.failed userId=$userId interpretationId=$interpretationId reason=$reason" }
            Unit
        }

    companion object {
        private const val USERS = "users"
        private const val DREAMS = "dreams"
    }
}
