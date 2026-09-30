package common.model.user

import com.google.cloud.Timestamp
import common.model.dream.DreamReads
import common.model.reiki.ReikiReads
import common.model.tarot.TarotReads

data class User(
    val userId: String = "",
    val name: String = "",
    /** First name, as handleUserSession (web and mobile) writes it. */
    val userName: String = "",
    val email: String = "",
    /** Set from the profile toggle or an email's unsubscribe link. */
    val marketingOptOut: Boolean = false,
    val tarot: TarotReads? = null,
    val dream: DreamReads? = null,
    val reiki: ReikiReads? = null,
    val createdAt: Timestamp? = null,
)
