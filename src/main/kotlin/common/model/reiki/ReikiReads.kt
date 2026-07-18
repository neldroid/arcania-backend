package common.model.reiki

/**
 * Represents the available Reiki appointments for the user. Mirrors the
 * "reiki.appointmentsAmount" field written directly by StripeRoutes (on
 * checkout) and decremented by ReikiRoutes (on Make.com confirmation).
 */
data class ReikiReads(
    val appointmentsAmount: Long = 0,
)
