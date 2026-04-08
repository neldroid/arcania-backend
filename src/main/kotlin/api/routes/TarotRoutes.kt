package api.routes

import api.ApiKeyAuthPlugin
import api.routes.request.ReadTarotRequest
import api.routes.response.ReadTarotResponse
import domain.tarot.TarotService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

fun Route.tarotRoutes() {
    val tarotService by inject<TarotService>()

    route("/read-cards") {
        install(ApiKeyAuthPlugin)
        get {
            val request = call.receive<ReadTarotRequest>()

            val readingId = tarotService.retrieveTarotReading(
                userId = request.userId,
                userName = request.userName,
                cardsQuantity = request.cardsQuantity,
                question = request.question,
                themes = request.themes,
                emotions = request.emotions,
            )

            call.respond(HttpStatusCode.OK, ReadTarotResponse(readingId))
        }
    }
}