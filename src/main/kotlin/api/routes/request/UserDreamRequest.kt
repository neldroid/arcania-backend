package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class UserDreamRequest(
    /**
     * Ignored. The acting user comes from the verified ID token (see
     * api.verifiedUid); kept optional so older clients still deserialize.
     */
    val userId: String? = null,
    val userName: String? = null,
    val dreamDescription: String,
    val themes: List<String>? = null,
    val emotions: List<String>? = null,
)
