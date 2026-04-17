package common.model.tarot

import com.google.cloud.Timestamp

/**
 * Class to represent the available Tarot related services for the user
 */
data class TarotReads(
    val readingsAmount: Int = 0,
    val nextFreeReadingAt: Timestamp? = null,
)
