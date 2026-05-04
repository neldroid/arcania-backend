package api.routes

import api.ApiKeyAuthPlugin
import api.routes.request.InterpretDreamRequest
import api.routes.response.InterpretDreamResponse
import api.routes.response.ResponseType
import domain.dream.DreamService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.ktor.ext.inject
import java.util.*

fun Route.dreamRoutes() {
    val dreamService by inject<DreamService>()

    route("/interpret-dream") {
        install(ApiKeyAuthPlugin)
        get {
            val request = call.receive<InterpretDreamRequest>()

            coroutineScope {
                launch {
                    val interpretationId = UUID.randomUUID()

                    try {
                        dreamService.retrieveDreamInterpretation(
                            interpretationId = interpretationId,
                            userId = request.userId,
                            userName = request.userName,
                            dreamDescription = request.dreamDescription,
                            themes = request.themes,
                            emotions = request.emotions,
                        )

                        call.respond(
                            HttpStatusCode.Accepted,
                            InterpretDreamResponse(responseId = ResponseType.SUCCESS, interpretationId.toString()),
                        )
                    } catch (exception: Exception) {
                        call.respond(
                            HttpStatusCode.NotAcceptable,
                            InterpretDreamResponse(responseId = ResponseType.ERROR),
                        )
                        println("Background processing failed: ${exception.message}")
                    }
                }
            }
        }
    }
}
