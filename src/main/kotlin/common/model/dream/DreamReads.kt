package common.model.dream

import com.google.cloud.Timestamp

data class DreamReads(
    val readings: List<String> = emptyList(),
    val nextFreeReadingAt: Timestamp? = null,
)
