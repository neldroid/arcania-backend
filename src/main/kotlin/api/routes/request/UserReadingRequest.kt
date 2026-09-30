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
    /**
     * The querent ticked the art. 9.2.a GDPR box: their question may contain
     * special-category data (health, beliefs, sex life…) and they agree to it
     * being processed, including by the AI providers, to write the reading.
     */
    val sensitiveDataConsent: Boolean = false,
)