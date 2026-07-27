package api.routes.request

import kotlinx.serialization.Serializable

@Serializable
data class ReadTarotRequest(
    val userId: String,
    val userName: String,
    val readingType: String,
    val question: String = "",
    val isForAnotherPerson: Boolean = false,
    /** Human-readable theme the querent picked in the wizard, if any — used by
     *  the intake analyst to check the question matches the selected theme. */
    val topic: String? = null,
    val subtopic: String? = null,
)