package api.routes

import api.ApiKeyAuthPlugin
import api.routes.request.ReadTarotRequest
import api.routes.response.ReadTarotResponse
import api.routes.response.ResponseType
import domain.usecase.CreateTarotReadingUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.util.UUID

private val log = KotlinLogging.logger {}

fun Route.tarotRoutes() {
    val useCase by inject<CreateTarotReadingUseCase>()

    route("/read-cards") {
        install(ApiKeyAuthPlugin)
        get {
            val request = call.receive<ReadTarotRequest>()
            val readingId = UUID.randomUUID()

            call.respond(
                HttpStatusCode.Accepted,
                ReadTarotResponse(responseId = ResponseType.SUCCESS, readingId.toString()),
            )

            call.application.launch {
                try {
                    useCase.execute(
                        CreateTarotReadingUseCase.Command(
                            readingId = readingId,
                            userId = request.userId,
                            userName = request.userName,
                            cardsQuantity = request.cardsQuantity,
                            question = request.question,
                            themes = request.themes,
                            emotions = request.emotions,
                            isForAnotherPerson = request.isForAnotherPerson,
                        )
                    )
                } catch (e: Exception) {
                    log.error(e) { "tarot.background.failed readingId=$readingId" }
                }
            }
        }
    }
}
