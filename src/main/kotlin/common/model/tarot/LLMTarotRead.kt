package common.model.tarot

import com.google.cloud.Timestamp
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LLMTarotRead(
    val opening: String = "",
    val cards: List<LLMCardReading> = emptyList(),
    val synthesis: String = "",
    val guidance: String = "",
    val summary: String = "",
    val question: String = "",
    @Contextual val createdAt: Timestamp? = null,
)

@Serializable
data class LLMCardReading(
    @SerialName("card_id")
    val cardId: Int = 0,
    @SerialName("card_name")
    val cardName: String = "",
    val position: String = "",
    @SerialName("is_inverted")
    val isInverted: Boolean = false,
    val interpretation: String = ""
)
