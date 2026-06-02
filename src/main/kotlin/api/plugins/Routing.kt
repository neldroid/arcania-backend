package api.plugins

import api.routes.bffRoutes
import api.routes.dreamRoutes
import api.routes.stripeRoutes
import api.routes.tarotRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        bffRoutes()
        tarotRoutes()
        dreamRoutes()
        stripeRoutes()
    }
}