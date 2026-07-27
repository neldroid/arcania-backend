package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class UserReadingRequest(
    val userId: String,
    val userName: String? = null,
    val readingId: String,
    val question: String = "",
    val isForAnotherPerson: Boolean = false,
    val topic: String? = null,
    val subtopic: String? = null,
)