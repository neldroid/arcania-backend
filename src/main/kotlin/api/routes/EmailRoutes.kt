package api.routes

import com.google.cloud.firestore.FieldValue
import data.firebase.UserRepository
import email.UnsubscribeLinks
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject

/**
 * Opt-out from marketing email.
 *
 * GET shows a confirmation page instead of unsubscribing outright, so mail
 * scanners that prefetch links don't unsubscribe people. POST performs it: the
 * confirmation button, and mail clients' one-click unsubscribe (RFC 8058),
 * both post to the same URL.
 */
fun Route.emailRoutes() {
    val userRepository by inject<UserRepository>()

    rateLimit(RateLimitName("public_email")) {
        route("/email/unsubscribe") {
            get {
                val uid = call.request.queryParameters["u"].orEmpty()
                val token = call.request.queryParameters["t"].orEmpty()
                if (!UnsubscribeLinks.verify(uid, token)) {
                    return@get call.respondText(page("Enlace no válido", "Este enlace de baja no es válido. Escríbenos a info@ezepiko.com y te daremos de baja."), ContentType.Text.Html, HttpStatusCode.BadRequest)
                }
                call.respondText(
                    page(
                        "Dejar de recibir emails",
                        """
                        <p>¿Quieres dejar de recibir emails con ofertas y lecturas gratuitas de Magestika?
                        Seguirás recibiendo los mensajes necesarios sobre tu cuenta y tus compras.</p>
                        <form method="post">
                          <button type="submit">Confirmar baja</button>
                        </form>
                        """.trimIndent(),
                    ),
                    ContentType.Text.Html,
                )
            }

            post {
                val uid = call.request.queryParameters["u"].orEmpty()
                val token = call.request.queryParameters["t"].orEmpty()
                if (!UnsubscribeLinks.verify(uid, token)) {
                    return@post call.respondText(page("Enlace no válido", "Este enlace de baja no es válido. Escríbenos a info@ezepiko.com y te daremos de baja."), ContentType.Text.Html, HttpStatusCode.BadRequest)
                }
                // A deleted account has nothing left to opt out of — still a success.
                if (userRepository.findUser(uid) != null) {
                    userRepository.update(
                        uid,
                        mapOf("marketingOptOut" to true, "marketingOptOutAt" to FieldValue.serverTimestamp()),
                    )
                }
                call.respondText(
                    page("Baja confirmada", "No volverás a recibir emails promocionales de Magestika. Puedes cambiarlo cuando quieras desde tu perfil."),
                    ContentType.Text.Html,
                )
            }
        }
    }
}

private fun page(title: String, body: String): String = """
    <!doctype html>
    <html lang="es">
    <head>
      <meta charset="utf-8" />
      <meta name="viewport" content="width=device-width, initial-scale=1" />
      <meta name="robots" content="noindex" />
      <title>$title · Magestika</title>
      <style>
        body { margin:0; background:#0a0510; color:#b9aecf; font-family: system-ui, -apple-system, 'Segoe UI', sans-serif; }
        main { max-width:480px; margin:15vh auto; padding:0 16px; line-height:1.7; font-size:15px; }
        h1 { font-family: Georgia, 'Times New Roman', serif; font-style:italic; font-weight:500; color:#f4eef9; }
        button { margin-top:12px; padding:12px 28px; border-radius:999px; border:0; background:#a855f7; color:#fff; font-size:15px; font-weight:600; cursor:pointer; }
      </style>
    </head>
    <body><main><h1>$title</h1>$body</main></body>
    </html>
""".trimIndent()
