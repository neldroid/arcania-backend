package common.model.user

import com.google.cloud.Timestamp
import common.model.dream.DreamReads
import common.model.reiki.ReikiReads
import common.model.tarot.TarotReads

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val tarot: TarotReads? = null,
    val dream: DreamReads? = null,
    val reiki: ReikiReads? = null,
    val createdAt: Timestamp? = null,
)
