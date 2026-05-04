package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class InterpretDreamRequest(
    val userId: String,
    val userName: String,
    val dreamDescription: String,
    val themes: List<String>,
    val emotions: List<String>,
)
