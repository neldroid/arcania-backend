package common.model.tarot

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LLMTarotRead(
    val opening: String,
    val cards: List<LLMCardReading>,
    val synthesis: String,
    val guidance: String,
    val summary: String
)

@Serializable
data class LLMCardReading(
    @SerialName("card_id")
    val cardId: Int,
    @SerialName("card_name")
    val cardName: String,
    val position: String,
    @SerialName("is_inverted")
    val isInverted: Boolean,
    val interpretation: String
)
