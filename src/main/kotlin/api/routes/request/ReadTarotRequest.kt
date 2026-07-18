package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class ReadTarotRequest(
    val userId: String,
    val userName: String,
    val readingType: String,
    val question: String = "",
    val isForAnotherPerson: Boolean = false,
)