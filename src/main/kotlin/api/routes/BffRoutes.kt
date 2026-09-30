package api.routes

import api.verifiedUid
import api.routes.request.InterpretDreamRequest
import api.routes.request.ReadTarotRequest
import api.routes.request.UserDreamRequest
import api.routes.request.UserReadingRequest
import api.routes.response.InterpretDreamResponse
import api.routes.response.ReadTarotResponse
import api.routes.response.ResponseType
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
import com.google.firebase.FirebaseApp
import com.google.cloud.firestore.FieldValue
import data.firebase.UserRepository
import org.koin.ktor.ext.inject

/**
 * Date of the consent wording in esoterico-frontend/lib/legal.ts
 * (AI_DISCLOSURE.sensitiveConsent). Bump both together when the text changes.
 */
private const val SENSITIVE_DATA_CONSENT_VERSION = "2026-09-30"

fun Route.bffRoutes() {
    val httpClient by inject<HttpClient>()
    val firebaseApp by inject<FirebaseApp>()
    val userRepository by inject<UserRepository>()

    rateLimit(RateLimitName("public_ai")) {
        route("/tarot") {
            post("/reading") {
                val uid = call.verifiedUid(firebaseApp)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, ReadTarotResponse(responseId = ResponseType.ERROR))
                val userRequest = call.receive<UserReadingRequest>()

                // Explicit consent is the only art. 9 GDPR basis that fits a
                // free-text question about someone's health or love life, so no
                // reading is written without it.
                if (!userRequest.sensitiveDataConsent) {
                    return@post call.respond(HttpStatusCode.BadRequest, ReadTarotResponse(responseId = ResponseType.ERROR))
                }
                // Art. 7.1 GDPR: be able to show when, and to which wording,
                // consent was given. Best-effort — the reading doesn't wait on it.
                runCatching {
                    userRepository.update(
                        uid,
                        mapOf(
                            "consents.sensitiveData" to mapOf(
                                "grantedAt" to FieldValue.serverTimestamp(),
                                "version" to SENSITIVE_DATA_CONSENT_VERSION,
                            )
                        ),
                    )
                }.onFailure { println("Could not record sensitive-data consent: ${it.javaClass.simpleName}") }

                val internalResponse = httpClient.get("http://localhost:8080/read-cards") {
                    header("X-Api-Key", System.getenv("TAROT_API_KEY"))
                    contentType(ContentType.Application.Json)
                    setBody(
                        ReadTarotRequest(
                            userId = uid,
                            userName = userRequest.userName ?: "",
                            readingType = userRequest.readingId,
                            question = userRequest.question,
                            isForAnotherPerson = userRequest.isForAnotherPerson,
                            topic = userRequest.topic,
                            subtopic = userRequest.subtopic,
                        )
                    )
                }

                val reading = internalResponse.body<ReadTarotResponse>()
                call.respond(internalResponse.status, reading)
            }
        }

        route("/dream") {
            post("/interpretation") {
                val uid = call.verifiedUid(firebaseApp)
                    ?: return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
                val userRequest = call.receive<UserDreamRequest>()

                val internalResponse = httpClient.get("http://localhost:8080/interpret-dream") {
                    header("X-Api-Key", System.getenv("TAROT_API_KEY"))
                    contentType(ContentType.Application.Json)
                    setBody(
                        InterpretDreamRequest(
                            userId = uid,
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