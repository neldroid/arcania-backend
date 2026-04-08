package api.routes.response

import kotlinx.serialization.Serializable

@Serializable
data class ReadTarotResponse(
    val readingId: String,
)
