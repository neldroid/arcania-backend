package api.routes

import api.plugins.appJson
import com.google.cloud.firestore.FieldValue
import com.stripe.net.Webhook
import data.firebase.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
                // The event's Stripe API version is newer than this SDK, so the typed
                // dataObjectDeserializer.getObject() returns an empty Optional. We only
                // need the checkout metadata, so read it straight from the raw payload —
                // this stays correct regardless of the Stripe API/SDK version gap.
                val metadata = appJson.parseToJsonElement(payload)
                    .jsonObject["data"]?.jsonObject
                    ?.get("object")?.jsonObject
                    ?.get("metadata")?.jsonObject

                val userId = metadata?.get("userId")?.jsonPrimitive?.contentOrNull
                val productType = metadata?.get("productType")?.jsonPrimitive?.contentOrNull
                val readingId = metadata?.get("readingId")?.jsonPrimitive?.contentOrNull

                if (userId != null) {
                    when (productType) {
                        "tarot" -> if (readingId != null) {
                            // Not arrayUnion: Firestore's arrayUnion de-dupes by value, so a
                            // second purchase of the same reading type would silently grant
                            // no extra credit. Appending the raw list preserves one token per purchase.
                            val currentReadings = userRepository.findUser(userId)?.tarot?.readings ?: emptyList()
                            userRepository.update(userId, mapOf("tarot.readings" to currentReadings + readingId))
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
