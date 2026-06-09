package domain.usecase

import domain.repository.StripeEventRepository
import domain.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val log = KotlinLogging.logger {}

/**
 * Processes a Stripe checkout.session.completed event.
 *
 * Idempotent: dedupes by [eventId] via [StripeEventRepository]. Late retries
 * from Stripe never double-credit a user.
 */
class ProcessStripeEventUseCase(
    private val users: UserRepository,
    private val events: StripeEventRepository,
) {

    data class CheckoutCompleted(
        val eventId: String,
        val userId: String,
        val productType: String,
        val readingId: String?,
    )

    suspend fun execute(event: CheckoutCompleted) {
        if (!events.markProcessedIfNew(event.eventId)) {
            log.info { "stripe.event.duplicate eventId=${event.eventId}" }
            return
        }

        when (event.productType) {
            "tarot" -> event.readingId?.let { users.grantTarotReading(event.userId, it) }
            "dream" -> users.grantDreamInterpretation(event.userId)
            "reiki" -> users.grantReikiAppointment(event.userId)
            else -> log.warn { "stripe.event.unknown_product type=${event.productType}" }
        }

        log.info {
            "stripe.event.processed eventId=${event.eventId} userId=${event.userId} " +
                    "product=${event.productType}"
        }
    }
}
