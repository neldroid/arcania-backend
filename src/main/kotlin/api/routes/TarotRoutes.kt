package api.routes

import api.ApiKeyAuthPlugin
import api.routes.request.ReadTarotRequest
import api.routes.response.ReadTarotResponse
import api.routes.response.ResponseType
import domain.tarot.TarotService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.util.*

fun Route.tarotRoutes() {
    val tarotService by inject<TarotService>()

    route("/read-cards") {
        install(ApiKeyAuthPlugin)
        get {
            val request = call.receive<ReadTarotRequest>()
            val readingId = UUID.randomUUID()

            call.respond(
                HttpStatusCode.Accepted,
                ReadTarotResponse(responseId = ResponseType.SUCCESS, readingId.toString())
            )

            application.launch {
                try {
                    tarotService.retrieveTarotReading(
                        readingId = readingId,
                        userId = request.userId,
                        userName = request.userName,
                        cardsQuantity = request.cardsQuantity,
                        question = request.question,
                        themes = request.themes,
                        emotions = request.emotions,
                        isForAnotherPerson = request.isForAnotherPerson,
                    )
                } catch (exception: Exception) {
                    println("Background processing failed: ${exception.message}")
                }
            }
        }
    }
}