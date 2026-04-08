package api.routes

import api.routes.request.ReadTarotRequest
import api.routes.request.UserReadingRequest
import api.routes.response.ReadTarotResponse
import common.model.tarot.LLMTarotRead
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.header
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.bffRoutes() {
    val httpClient by inject<HttpClient>()
    route("/tarot") {

        post("/reading") {
            val userRequest = call.receive<UserReadingRequest>()

            val internalResponse = httpClient.get("http://localhost:8080/read-cards") {
                header("X-Api-Key", System.getenv("TAROT_API_KEY"))
                contentType(ContentType.Application.Json)
                setBody(
                    ReadTarotRequest(
                        userId = userRequest.userId,
                        userName = userRequest.userName ?: "",
                        cardsQuantity = userRequest.cardsQuantity,
                        question = userRequest.question,
                        themes = userRequest.themes ?: emptyList(),
                        emotions = userRequest.emotions ?: emptyList(),
                    )
                )
            }

            val reading = internalResponse.body<ReadTarotResponse>()
            call.respond(HttpStatusCode.OK, reading)
        }
    }
}