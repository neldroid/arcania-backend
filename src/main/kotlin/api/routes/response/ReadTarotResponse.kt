package api.routes.response

import kotlinx.serialization.Serializable

@Serializable
data class ReadTarotResponse(
    val responseId: ResponseType,
    val readingId: String? = null,
)


enum class ResponseType {
    SUCCESS, ERROR, INVALID_INPUT
}