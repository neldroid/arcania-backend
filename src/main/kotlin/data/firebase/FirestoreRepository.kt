package data.firebase

import com.google.cloud.firestore.Firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class FirestoreRepository<T> (
    protected val firestore: Firestore,
    private val collectionName: String,
    private val clazz: Class<T>
) {
    protected val collection get() = firestore.collection(collectionName)

    suspend fun findById(id: String): T? = withContext(Dispatchers.IO) {
        collection.document(id).get().get()
            .takeIf { it.exists() }
            ?.toObject(clazz)
    }

    suspend fun findAll(): List<T> = withContext(Dispatchers.IO) {
        collection.get().get().documents
            .mapNotNull { it.toObject(clazz) }
    }

    suspend fun save(id: String, data: T): T = withContext(Dispatchers.IO) {
        collection.document(id).set(data as Any).get()
        data
    }

    suspend fun update(id: String, fields: Map<String, Any>): Boolean = withContext(Dispatchers.IO) {
        collection.document(id).update(fields).get()
        true
    }

    suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        collection.document(id).delete().get()
        true
    }

    // Access a subcollection under a specific document
    protected fun subCollection(documentId: String, subCollectionName: String) =
        collection.document(documentId).collection(subCollectionName)
}