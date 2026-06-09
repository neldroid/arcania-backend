package data.firebase

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import domain.repository.StripeEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Firestore-backed idempotency store.
 *
 * Uses [com.google.cloud.firestore.WriteResult]'s create-only semantics:
 * `document.create(...)` throws `ALREADY_EXISTS` for any duplicate. That makes
 * dedup atomic without a transaction.
 */
class FirestoreStripeEventRepository(
    private val firestore: Firestore,
) : StripeEventRepository {

    override suspend fun markProcessedIfNew(eventId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            firestore.collection(COLLECTION).document(eventId).create(
                mapOf("processedAt" to FieldValue.serverTimestamp())
            ).get()
            true
        } catch (e: Exception) {
            // ALREADY_EXISTS — event was already processed.
            false
        }
    }

    companion object {
        private const val COLLECTION = "stripe_processed_events"
    }
}
