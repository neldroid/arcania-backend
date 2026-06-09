package api.routes

import api.ApiKeyAuthPlugin
import api.routes.request.InterpretDreamRequest
import api.routes.response.InterpretDreamResponse
import api.routes.response.ResponseType
import domain.usecase.InterpretDreamUseCase
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

fun Route.dreamRoutes() {
    val useCase by inject<InterpretDreamUseCase>()

    route("/interpret-dream") {
        install(ApiKeyAuthPlugin)
        get {
            val request = call.receive<InterpretDreamRequest>()
            val interpretationId = UUID.randomUUID()

            call.respond(
                HttpStatusCode.Accepted,
                InterpretDreamResponse(responseId = ResponseType.SUCCESS, interpretationId.toString()),
            )

            call.application.launch {
                try {
                    useCase.execute(
                        InterpretDreamUseCase.Command(
                            interpretationId = interpretationId,
                            userId = request.userId,
                            userName = request.userName,
                            dreamDescription = request.dreamDescription,
                            themes = request.themes,
                            emotions = request.emotions,
                        )
                    )
                } catch (e: Exception) {
                    log.error(e) { "dream.background.failed interpretationId=$interpretationId" }
                }
            }
        }
    }
}
