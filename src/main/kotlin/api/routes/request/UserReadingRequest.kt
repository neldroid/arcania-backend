package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class UserReadingRequest(
    val userName: String? = null,
    val cardsQuantity: Int,
    val question: String,
    val themes: List<String>? = null,
    val emotions: List<String>? = null,
)