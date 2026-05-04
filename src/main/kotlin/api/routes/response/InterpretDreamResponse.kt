package api.routes.response

import kotlinx.serialization.Serializable

@Serializable
data class InterpretDreamResponse(
    val responseId: ResponseType,
    val interpretationId: String? = null,
)
