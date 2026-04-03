package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class ReadTarotRequest(
    val userId: String,
    val userName: String,
    val question: String,
    val themes: List<String>,
    val emotions: List<String>,
    val cardsQuantity: Int,
)
