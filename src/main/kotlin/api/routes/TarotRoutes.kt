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

            // Verify the user actually has a credit for this reading BEFORE
            // responding — only once that's confirmed do we tell the client
            // "success" and let the LLM call + Firestore write run in the
            // background. This is the one part of the flow worth the wait:
            // it's a single Firestore read, not the slow agent call.
            val credit = try {
                tarotService.resolveCredit(request.userId, request.readingType)
            } catch (exception: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ReadTarotResponse(responseId = ResponseType.ERROR))
                return@get
            } catch (exception: IllegalStateException) {
                call.respond(HttpStatusCode.PaymentRequired, ReadTarotResponse(responseId = ResponseType.ERROR))
                return@get
            }

            // Inspect the input before committing to a reading. This is a second
            // synchronous step worth the wait: it lets us reject junk/joke input
            // up front — no credit is consumed (consumption only happens inside
            // generateReading) and the client gets actionable feedback instead of
            // a nonsense reading. Fail-open: analyzeQuestion never throws.
            val analysis = tarotService.analyzeQuestion(
                question = request.question,
                topic = request.topic,
                subtopic = request.subtopic,
                isForAnotherPerson = request.isForAnotherPerson,
            )

            if (analysis.isTrash) {
                call.respond(
                    HttpStatusCode.UnprocessableEntity,
                    ReadTarotResponse(responseId = ResponseType.INVALID_INPUT)
                )
                return@get
            }

            call.respond(
                HttpStatusCode.Accepted,
                ReadTarotResponse(responseId = ResponseType.SUCCESS, readingId.toString())
            )

            application.launch {
                try {
                    tarotService.generateReading(
                        readingId = readingId,
                        userId = request.userId,
                        userName = request.userName,
                        credit = credit,
                        question = request.question,
                        isForAnotherPerson = request.isForAnotherPerson,
                        analysis = analysis,
                    )
                } catch (exception: Exception) {
                    println("Background processing failed: ${exception.message}")
                }
            }
        }
    }
}