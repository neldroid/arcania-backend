package api.routes

import api.routes.request.InterpretDreamRequest
import api.routes.request.ReadTarotRequest
import api.routes.request.UserDreamRequest
import api.routes.request.UserReadingRequest
import api.routes.response.InterpretDreamResponse
import api.routes.response.ReadTarotResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.bffRoutes() {
    val httpClient by inject<HttpClient>()

    rateLimit(RateLimitName("public_ai")) {
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
                            isForAnotherPerson = userRequest.isForAnotherPerson,
                        )
                    )
                }

                val reading = internalResponse.body<ReadTarotResponse>()
                call.respond(HttpStatusCode.OK, reading)
            }

            post("/key") {
                // Reading for the "KEY" pay Tarot
                val userRequest = call.receive<UserReadingRequest>()

//            val internalResponse = httpClient.get("http://localhost:8080/read-cards") {
//                header("X-Api-Key", System.getenv("TAROT_API_KEY"))
//                contentType(ContentType.Application.Json)
//                setBody()
//            }
            }
        }

        route("/dream") {
            post("/interpretation") {
                val userRequest = call.receive<UserDreamRequest>()

                val internalResponse = httpClient.get("http://localhost:8080/interpret-dream") {
                    header("X-Api-Key", System.getenv("TAROT_API_KEY"))
                    contentType(ContentType.Application.Json)
                    setBody(
                        InterpretDreamRequest(
                            userId = userRequest.userId,
                            userName = userRequest.userName ?: "",
                            dreamDescription = userRequest.dreamDescription,
                            themes = userRequest.themes ?: emptyList(),
                            emotions = userRequest.emotions ?: emptyList(),
                        )
                    )
                }

                val interpretation = internalResponse.body<InterpretDreamResponse>()
                call.respond(HttpStatusCode.OK, interpretation)
            }
        }
    }
}