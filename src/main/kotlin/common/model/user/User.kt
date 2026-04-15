package common.model.user

import common.model.tarot.TarotReads

data class User(
    val id: String = "",
    val name: String = "",
    val tarot: TarotReads? = null,
)
