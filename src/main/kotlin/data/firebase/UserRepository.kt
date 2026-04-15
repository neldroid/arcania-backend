package data.firebase

import com.google.cloud.firestore.Firestore
import common.model.user.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(firestore: Firestore) : FirestoreRepository<User>(
    firestore = firestore,
    collectionName = "users",
    clazz = User::class.java,
) {

    suspend fun findUser(userId: String) = withContext(Dispatchers.IO) {
        findById(userId)
    }

}