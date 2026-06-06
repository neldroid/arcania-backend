package api.routes

import com.google.cloud.firestore.FieldValue
import com.google.cloud.firestore.Firestore
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject

@Serializable
private data class ReikiConfirmRequest(val email: String)

fun Route.reikiRoutes() {
    val firestore by inject<Firestore>()

    route("/reiki") {
        post("/confirm") {
            val expected = System.getenv("MAKE_WEBHOOK_SECRET")
                ?: return@post call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to "Server misconfigured")
                )

            val secret = call.request.headers["x-make-secret"]
            if (secret != expected) {
                return@post call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Unauthorized"))
            }

            val body = call.receive<ReikiConfirmRequest>()
            if (body.email.isBlank()) {
                return@post call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing email"))
            }

            val userId = withContext(Dispatchers.IO) {
                val snap = firestore.collection("users")
                    .whereEqualTo("email", body.email)
                    .limit(1)
                    .get()
                    .get()
                if (snap.isEmpty) null
                else snap.documents[0].getString("userId")
            }

            if (userId == null) {
                return@post call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
            }

            withContext(Dispatchers.IO) {
                val ref = firestore.collection("users").document(userId)
                val snap = ref.get().get()
                val current = (snap.get("reiki.appointmentsAmount") as? Number)?.toLong() ?: 0L
                if (current > 0) {
                    ref.update("reiki.appointmentsAmount", FieldValue.increment(-1)).get()
                }
            }

            call.respond(HttpStatusCode.OK, mapOf("ok" to true))
        }
    }
}
