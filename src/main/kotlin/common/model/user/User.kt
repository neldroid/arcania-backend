package common.model.user

import com.google.cloud.Timestamp
import common.model.tarot.TarotReads

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val tarot: TarotReads? = null,
    val createdAt: Timestamp? = null,
)
