package api.routes

import com.google.cloud.firestore.FieldValue
import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import data.firebase.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.stripeRoutes() {
    val userRepository by inject<UserRepository>()

    route("/stripe") {
        post("/webhook") {
            val payload = call.receiveText()
            val sigHeader = call.request.headers["Stripe-Signature"]
            val secret = System.getenv("STRIPE_WEBHOOK_SECRET")
                ?: return@post call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Webhook secret not configured"))

            val event = try {
                Webhook.constructEvent(payload, sigHeader, secret)
            } catch (e: Exception) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid signature"))
            }

            if (event.type == "checkout.session.completed") {
                val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
                val userId = session?.metadata?.get("userId")
                val productType = session?.metadata?.get("productType")
                val readingId = session?.metadata?.get("readingId")

                if (userId != null) {
                    when (productType) {
                        "tarot" -> if (readingId != null) {
                            userRepository.update(userId, mapOf("tarot.readings" to FieldValue.arrayUnion(readingId)))
                        }
                        "reiki" -> {
                            userRepository.update(userId, mapOf("reiki.appointmentsAmount" to FieldValue.increment(1)))
                        }
                        "dream" -> {
                            userRepository.update(userId, mapOf("dream.readings" to FieldValue.arrayUnion("dream")))
                        }
                    }
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("received" to true))
        }
    }
}
