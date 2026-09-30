package email

import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Signed one-click unsubscribe links for marketing email.
 *
 * Art. 21 LSSI and CAN-SPAM both require every commercial email to carry a
 * simple, free way to opt out. The link has to work without signing in, so it
 * carries the uid plus an HMAC of it: nobody can unsubscribe somebody else by
 * editing the uid, and there is no token table to keep.
 */
object UnsubscribeLinks {

    private val secret: ByteArray by lazy {
        (System.getenv("EMAIL_UNSUBSCRIBE_SECRET")
            ?: error("EMAIL_UNSUBSCRIBE_SECRET environment variable is not set"))
            .toByteArray()
    }

    private val publicBaseUrl: String by lazy {
        (System.getenv("PUBLIC_BACKEND_URL") ?: "https://arcania-backend.onrender.com").trimEnd('/')
    }

    private fun sign(uid: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret, "HmacSHA256"))
        return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(uid.toByteArray()))
    }

    /** Constant-time check that [token] was issued for [uid]. */
    fun verify(uid: String, token: String): Boolean =
        uid.isNotBlank() && MessageDigest.isEqual(sign(uid).toByteArray(), token.toByteArray())

    /** URL for both the visible link and the RFC 8058 List-Unsubscribe header. */
    fun urlFor(uid: String): String {
        val encodedUid = java.net.URLEncoder.encode(uid, Charsets.UTF_8)
        return "$publicBaseUrl/email/unsubscribe?u=$encodedUid&t=${sign(uid)}"
    }

    /** Headers that let mail clients show their own "unsubscribe" button (RFC 2369 / 8058). */
    fun headersFor(uid: String): Map<String, String> = mapOf(
        "List-Unsubscribe" to "<${urlFor(uid)}>",
        "List-Unsubscribe-Post" to "List-Unsubscribe=One-Click",
    )
}
