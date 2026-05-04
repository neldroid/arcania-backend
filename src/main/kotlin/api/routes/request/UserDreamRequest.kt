package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class UserDreamRequest(
    val userId: String,
    val userName: String? = null,
    val dreamDescription: String,
    val themes: List<String>? = null,
    val emotions: List<String>? = null,
)
