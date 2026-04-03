import api.plugins.configureRouting
import api.plugins.configureSerialization
import api.routes.request.ReadTarotRequest
import common.di.appModule
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.requestvalidation.ValidationResult
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>) {
    EngineMain.main(args)
}

@OptIn(ExperimentalSerializationApi::class)
fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }

    install(RequestValidation) {
        validate<ReadTarotRequest> { request ->
            val errors = mutableListOf<String>()

            if (request.cardsQuantity !in listOf(1, 3)) {
                errors.add("cardsQuantity must be 1 or 3")
            }
            if (request.question.isBlank()) {
                errors.add("question is required and cannot be empty")
            }

            if (errors.isEmpty()) ValidationResult.Valid
            else ValidationResult.Invalid(errors.joinToString(", "))
        }
    }

    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to cause.reasons.joinToString(", "))
            )
        }

        exception<ContentTransformationException> { call, cause ->
            val message = when (val root = cause.cause) {
                is MissingFieldException -> "Missing required field: ${root.missingFields.joinToString()}"
                is SerializationException -> "Invalid field value: ${root.message}"
                else -> "Invalid request body"
            }
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to message))
        }

        // Safety net for anything else unexpected
        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled exception", cause)
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Unexpected error"))
        }
    }

    configureSerialization()
    configureRouting()
}