package data.firebase

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import common.model.user.User
import domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FirestoreUserRepository(
    private val firestore: Firestore,
) : UserRepository {

    private val users get() = firestore.collection(USERS)

    override suspend fun findUser(userId: String): User? = withContext(Dispatchers.IO) {
        users.document(userId).get().get()
            .takeIf { it.exists() }
            ?.toObject(User::class.java)
    }

    override suspend fun findUserIdByEmail(email: String): String? = withContext(Dispatchers.IO) {
        val snap = users.whereEqualTo("email", email).limit(1).get().get()
        if (snap.isEmpty) null else snap.documents[0].getString("userId")
    }

    override suspend fun grantTarotReading(userId: String, readingType: String) =
        withContext(Dispatchers.IO) {
            users.document(userId).update(
                mapOf("tarot.readings" to FieldValue.arrayUnion(readingType))
            ).get()
            Unit
        }

    override suspend fun grantDreamInterpretation(userId: String) = withContext(Dispatchers.IO) {
        users.document(userId).update(
            mapOf("dream.readings" to FieldValue.arrayUnion("dream"))
        ).get()
        Unit
    }

    override suspend fun grantReikiAppointment(userId: String) = withContext(Dispatchers.IO) {
        users.document(userId).update(
            mapOf("reiki.appointmentsAmount" to FieldValue.increment(1))
        ).get()
        Unit
    }

    override suspend fun consumeReikiAppointment(userId: String) = withContext(Dispatchers.IO) {
        val ref = users.document(userId)
        val snap = ref.get().get()
        val current = (snap.get("reiki.appointmentsAmount") as? Number)?.toLong() ?: 0L
        if (current > 0) {
            ref.update("reiki.appointmentsAmount", FieldValue.increment(-1)).get()
        }
        Unit
    }

    companion object {
        private const val USERS = "users"
    }
}
