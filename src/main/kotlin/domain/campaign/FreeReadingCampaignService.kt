package domain.campaign

import com.google.cloud.firestore.FieldValue
import data.firebase.UserRepository
import domain.tarot.ReadingCatalog
import domain.tarot.freeReadingFlag
import email.FreeReadingEmailTemplate
import email.ResendEmailClient
import kotlinx.serialization.Serializable

/** Recognized values for [FreeReadingCampaignService.run]'s `filter` param. */
object CampaignFilter {
    /** Users with no credits sitting in tarot.readings right now — free, purchased, or otherwise. */
    const val NO_READINGS = "no-readings"
}

@Serializable
data class CampaignResult(
    val matched: Int,
    val emailed: Int,
    val failedUserIds: List<String>,
)

/**
 * Grants a free-reading credit to every user matching [filter], then emails
 * them a CTA straight to that reading. The credit is granted before the email
 * is sent (so a user who somehow gets the email always has the credit
 * waiting), and a per-user email failure doesn't stop the rest of the batch.
 */
class FreeReadingCampaignService(
    private val userRepository: UserRepository,
    private val emailClient: ResendEmailClient,
) {

    private val frontendUrl: String = System.getenv("FRONTEND_URL")
        ?: error("FRONTEND_URL environment variable is not set")

    suspend fun run(filter: String, readingType: String): CampaignResult {
        val reading = ReadingCatalog.get(readingType)
            ?: throw IllegalArgumentException("Unknown reading type: $readingType")

        val candidates = when (filter) {
            CampaignFilter.NO_READINGS -> userRepository.findAll()
                .filter { it.tarot?.readings.isNullOrEmpty() && it.email.isNotBlank() }
            else -> throw IllegalArgumentException("Unknown filter: $filter")
        }

        val freeFlag = freeReadingFlag(readingType)
        val ctaUrl = "$frontendUrl/tarot/$readingType"
        val failed = mutableListOf<String>()
        var emailed = 0

        for (user in candidates) {
            userRepository.update(user.userId, mapOf("tarot.readings" to FieldValue.arrayUnion(freeFlag)))

            val sent = try {
                emailClient.send(
                    to = user.email,
                    subject = FreeReadingEmailTemplate.subject(reading),
                    html = FreeReadingEmailTemplate.html(userName = user.name, reading = reading, ctaUrl = ctaUrl),
                )
            } catch (e: Exception) {
                false
            }

            if (sent) emailed++ else failed.add(user.userId)
        }

        return CampaignResult(matched = candidates.size, emailed = emailed, failedUserIds = failed)
    }
}
