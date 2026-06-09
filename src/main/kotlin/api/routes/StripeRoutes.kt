package api.routes

import com.stripe.model.checkout.Session
import com.stripe.net.Webhook
import common.config.AppConfig
import domain.usecase.ProcessStripeEventUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}

fun Route.stripeRoutes() {
    val useCase by inject<ProcessStripeEventUseCase>()
    val config by inject<AppConfig>()

    route("/stripe") {
        post("/webhook") {
            val payload = call.receiveText()
            val sigHeader = call.request.headers["Stripe-Signature"]

            val event = try {
                Webhook.constructEvent(payload, sigHeader, config.stripeWebhookSecret)
            } catch (e: Exception) {
                log.warn { "stripe.webhook.invalid_signature" }
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid signature"))
            }

            if (event.type == "checkout.session.completed") {
                val session = event.dataObjectDeserializer.`object`.orElse(null) as? Session
                val userId = session?.metadata?.get("userId")
                val productType = session?.metadata?.get("productType")
                val readingId = session?.metadata?.get("readingId")

                if (userId != null && productType != null) {
                    useCase.execute(
                        ProcessStripeEventUseCase.CheckoutCompleted(
                            eventId = event.id,
                            userId = userId,
                            productType = productType,
                            readingId = readingId,
                        )
                    )
                } else {
                    log.warn { "stripe.webhook.missing_metadata eventId=${event.id}" }
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("received" to true))
        }
    }
}
