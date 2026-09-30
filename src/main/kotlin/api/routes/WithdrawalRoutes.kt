package api.routes

import email.ResendEmailClient
import email.WithdrawalEmailTemplate
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
data class WithdrawalRequest(
    val name: String,
    val email: String,
    val orderDescription: String,
    val purchaseDate: String = "",
)

private val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

/**
 * The online withdrawal function (art. 11a Directive 2011/83/EU, as amended by
 * Directive (EU) 2023/2673). It needs no account — the right belongs to the
 * consumer whether or not they can still sign in — so it is identified by
 * email and rate limited instead.
 */
fun Route.withdrawalRoutes() {
    val emailClient by inject<ResendEmailClient>()
    val operatorEmail = System.getenv("WITHDRAWAL_NOTIFY_EMAIL") ?: "info@ezepiko.com"

    rateLimit(RateLimitName("public_email")) {
        post("/withdrawal") {
            val request = runCatching { call.receive<WithdrawalRequest>() }.getOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_body"))

            val name = request.name.trim()
            val email = request.email.trim()
            val order = request.orderDescription.trim()
            val purchaseDate = request.purchaseDate.trim()

            val valid = name.length in 1..200 &&
                email.length <= 254 && EMAIL_PATTERN.matches(email) &&
                order.length in 1..1000 &&
                purchaseDate.length <= 100
            if (!valid) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_fields"))
            }

            val statement = WithdrawalEmailTemplate.Statement(
                name = name,
                email = email,
                orderDescription = order,
                purchaseDate = purchaseDate,
                receivedAt = WithdrawalEmailTemplate.receivedNow(),
            )

            // The operator copy is the one that must not be lost: without it
            // nobody processes the refund, so its failure fails the request.
            val operatorSent = runCatching {
                emailClient.send(
                    to = operatorEmail,
                    subject = WithdrawalEmailTemplate.operatorSubject(statement),
                    html = WithdrawalEmailTemplate.operatorHtml(statement),
                    replyTo = email,
                )
            }.getOrDefault(false)
            if (!operatorSent) {
                return@post call.respond(HttpStatusCode.BadGateway, mapOf("error" to "not_sent"))
            }

            val acknowledged = runCatching {
                emailClient.send(
                    to = email,
                    subject = WithdrawalEmailTemplate.acknowledgementSubject(),
                    html = WithdrawalEmailTemplate.acknowledgementHtml(statement),
                )
            }.getOrDefault(false)

            call.respond(HttpStatusCode.OK, mapOf("received" to true, "acknowledged" to acknowledged))
        }
    }
}
