package email

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable

/**
 * Thin wrapper over Resend's REST API (https://resend.com/docs/api-reference/emails/send-email).
 * No official Resend SDK dependency needed — it's a single JSON POST, and the
 * app already carries a configured Ktor [HttpClient].
 */
class ResendEmailClient(private val httpClient: HttpClient) {

    private val apiKey = System.getenv("RESEND_API_KEY")
        ?: error("RESEND_API_KEY environment variable is not set")

    /**
     * "Name <email>" sender identity. Until a domain is verified in Resend,
     * set this to Resend's sandbox sender ("Arcania <onboarding@resend.dev>"),
     * which works with no setup but only delivers to the Resend account's own
     * verified address.
     */
    private val fromAddress = System.getenv("RESEND_FROM_EMAIL")
        ?: error("RESEND_FROM_EMAIL environment variable is not set")

    @Serializable
    private data class ResendEmailRequest(
        val from: String,
        val to: List<String>,
        val subject: String,
        val html: String,
    )

    /** @return true if Resend accepted the email for delivery. */
    suspend fun send(to: String, subject: String, html: String): Boolean {
        val response = httpClient.post("https://api.resend.com/emails") {
            header("Authorization", "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(ResendEmailRequest(from = fromAddress, to = listOf(to), subject = subject, html = html))
        }
        return response.status.isSuccess()
    }
}
