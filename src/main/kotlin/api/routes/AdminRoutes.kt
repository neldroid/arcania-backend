package api.routes

import domain.campaign.FreeReadingCampaignService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
private data class FreeReadingCampaignRequest(val filter: String, val readingType: String)

fun Route.adminRoutes() {
    val campaignService by inject<FreeReadingCampaignService>()

    route("/admin") {
        post("/campaigns/free-reading") {
            val expected = System.getenv("ADMIN_API_KEY")
                ?: return@post call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Server misconfigured"))

            val key = call.request.headers["X-Admin-Key"]
            if (key != expected) {
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
            }

            val body = call.receive<FreeReadingCampaignRequest>()

            val result = try {
                campaignService.run(filter = body.filter, readingType = body.readingType)
            } catch (e: IllegalArgumentException) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid request")))
            }

            call.respond(HttpStatusCode.OK, result)
        }
    }
}
