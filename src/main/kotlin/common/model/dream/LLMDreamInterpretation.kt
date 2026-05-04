package common.model.dream

import com.google.cloud.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LLMDreamInterpretation(
    val overview: String = "",
    val symbols: List<LLMDreamSymbol> = emptyList(),
    val emotional: String = "",
    val guidance: String = "",
    val summary: String = "",
    @Contextual val createdAt: Timestamp? = null,
)

@Serializable
data class LLMDreamSymbol(
    val symbol: String = "",
    val meaning: String = "",
    @SerialName("archetype")
    val archetype: String = "",
)
