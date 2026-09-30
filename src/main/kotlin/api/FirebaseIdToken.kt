package api

import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The Firebase uid of whoever made this call, taken from a verified ID token in
 * `Authorization: Bearer <token>` — or null when the header is missing, the
 * token is malformed, expired, revoked-by-expiry or issued for another project.
 *
 * Public routes must derive the acting user from this and never from a
 * `userId` in the body: a body field is whatever the caller typed, so trusting
 * it would let anyone spend another person's credits or write into their
 * account.
 */
suspend fun ApplicationCall.verifiedUid(firebaseApp: FirebaseApp): String? {
    val token = request.header("Authorization")
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.substring("Bearer ".length)
        ?.trim()
        ?.takeIf { it.isNotEmpty() }
        ?: return null

    return try {
        // verifyIdToken fetches (and caches) Google's public keys, so keep it
        // off the event loop.
        withContext(Dispatchers.IO) {
            FirebaseAuth.getInstance(firebaseApp).verifyIdToken(token).uid
        }
    } catch (_: FirebaseAuthException) {
        null
    } catch (_: IllegalArgumentException) {
        null
    }
}
