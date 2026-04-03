package api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.header
import io.ktor.server.response.respond

val API_KEY: String by lazy {
    System.getenv("TAROT_API_KEY")
        ?: error("TAROT_API_KEY environment variable is not set")
}

val ApiKeyAuthPlugin = createRouteScopedPlugin("ApiKeyAuth") {
    onCall { call ->
        val key = call.request.header("X-Api-Key")
        if (key == null || key != API_KEY) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing API key"))
            return@onCall
        }
    }
}