package api.routes

import common.config.AppConfig
import domain.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

private val log = KotlinLogging.logger {}

@Serializable
private data class ReikiConfirmRequest(val email: String)

fun Route.reikiRoutes() {
    val users by inject<UserRepository>()
    val config by inject<AppConfig>()

    route("/reiki") {
        post("/confirm") {
            val secret = call.request.headers["x-make-secret"]
            if (secret != config.makeWebhookSecret) {
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
            }

            val body = call.receive<ReikiConfirmRequest>()
            if (body.email.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing email"))
            }

            val userId = users.findUserIdByEmail(body.email)
                ?: return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))

            users.consumeReikiAppointment(userId)
            log.info { "reiki.appointment.confirmed userId=$userId" }
            call.respond(HttpStatusCode.OK, mapOf("ok" to true))
        }
    }
}
