package api

import common.config.AppConfig
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.request.header
import io.ktor.server.response.respond
import org.koin.mp.KoinPlatform

val ApiKeyAuthPlugin = createRouteScopedPlugin("ApiKeyAuth") {
    val expected = KoinPlatform.getKoin().get<AppConfig>().internalApiKey
    onCall { call ->
        val key = call.request.header("X-Api-Key")
        if (key == null || key != expected) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing API key"))
            return@onCall
        }
    }
}
