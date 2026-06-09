package domain.repository

/**
 * Idempotency store for Stripe webhook events.
 *
 * Stripe retries webhooks aggressively; the same `event.id` can be delivered
 * multiple times. Implementations MUST guarantee that [markProcessedIfNew]
 * returns `true` for exactly one caller per event.id.
 */
interface StripeEventRepository {
    suspend fun markProcessedIfNew(eventId: String): Boolean
}
