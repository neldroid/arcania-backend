package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class UserReadingRequest(
    /**
     * Ignored. The acting user comes from the verified ID token (see
     * api.verifiedUid); kept optional so older clients still deserialize.
     */
    val userId: String? = null,
    val userName: String? = null,
    val readingId: String,
    val question: String = "",
    val isForAnotherPerson: Boolean = false,
    val topic: String? = null,
    val subtopic: String? = null,
)