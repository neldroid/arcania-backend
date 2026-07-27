package domain.tarot

import agent.QuestionIntakeAgent
import agent.TarotReadingAgent
import com.google.cloud.Timestamp
import common.model.tarot.LLMTarotRead
import common.model.tarot.QuestionAnalysis
import data.firebase.TarotReadingRepository
import data.firebase.UserRepository
import kotlinx.serialization.json.Json
import java.util.UUID

class TarotService(
    private val tarotReadingRepository: TarotReadingRepository,
    private val userRepository: UserRepository,
) {

    companion object {
        /** Granted to every new user on signup; redeemable on any one reading. See services/userService.ts (frontend). */
        private const val FULL_FREE_FLAG = "FULL_FREE"
    }

    /** What [resolveCredit] found: the reading to generate and the token that pays for it. */
    data class ReadingCredit(val definition: ReadingDefinition, val creditToken: String)

    /**
     * Resolves the reading definition and finds which credit token covers it,
     * WITHOUT consuming it or generating anything. Callers must await this
     * — and only respond "success" to the client once it returns — so an
     * unentitled or unknown-reading request never gets a premature success;
     * the actual LLM call + Firestore write ([generateReading]) can then run
     * safely in the background after that.
     *
     * Priority mirrors getRedeemableFreeFlag in the frontend's lib/pricing.ts:
     * a purchased token matching the reading type first (so a real purchase
     * is spent before a free credit is), then its type-specific free-promo
     * flag, then the generic FULL_FREE flag.
     *
     * @throws IllegalArgumentException if readingType isn't a known reading
     * @throws IllegalStateException if the user has no credit for it
     */
    suspend fun resolveCredit(userId: String, readingType: String): ReadingCredit {
        val definition = ReadingCatalog.get(readingType)
            ?: throw IllegalArgumentException("Unknown reading type: $readingType")

        val userReadings = userRepository.findUser(userId)?.tarot?.readings ?: emptyList()
        val freeFlag = freeReadingFlag(readingType)
        val creditToken = userReadings.firstOrNull { it.equals(readingType, ignoreCase = true) }
            ?: userReadings.firstOrNull { it.equals(freeFlag, ignoreCase = true) }
            ?: userReadings.firstOrNull { it.equals(FULL_FREE_FLAG, ignoreCase = true) }
            ?: throw IllegalStateException("Not enough readings left")

        return ReadingCredit(definition, creditToken)
    }

    /**
     * Inspects the querent's input before generating anything (see
     * [QuestionIntakeAgent]). The route awaits this and rejects junk/joke input
     * up front so no credit is spent; the same result is then fed into the
     * reading agent to steer language variant, tone and framing. Fail-open: any
     * problem yields [QuestionAnalysis.NEUTRAL], never a wrongful rejection.
     */
    suspend fun analyzeQuestion(
        question: String,
        topic: String?,
        subtopic: String?,
        isForAnotherPerson: Boolean,
    ): QuestionAnalysis {
        val selectedTheme = listOfNotNull(
            topic?.takeIf { it.isNotBlank() },
            subtopic?.takeIf { it.isNotBlank() },
        ).joinToString(" / ").ifBlank { null }

        return QuestionIntakeAgent.analyze(
            question = question,
            selectedTheme = selectedTheme,
            isForAnotherPerson = isForAnotherPerson,
        )
    }

    /**
     * Generates and stores the reading for a credit already confirmed by
     * [resolveCredit]. The BE is the sole owner of consuming that credit
     * token: it's removed from tarot.readings once the reading is saved.
     */
    suspend fun generateReading(
        readingId: UUID,
        userId: String,
        userName: String,
        credit: ReadingCredit,
        question: String,
        isForAnotherPerson: Boolean = false,
        analysis: QuestionAnalysis = QuestionAnalysis.NEUTRAL,
    ) {
        val (definition, creditToken) = credit
        // 1) Get the previous readings for the userId (skip if reading is for another person)
        val previousReadings: List<String> = if (isForAnotherPerson) emptyList()
        else tarotReadingRepository.getLastReadingSummaries(userId)

        // 2) Get the card/s for this session, one per spread position
        val cards: List<TarotCard> = TarotCardHelper.getCards(definition.positions)

        // 3) Get the reading answer from the agent
        val read = TarotReadingAgent.readCards(
            userName = userName,
            question = question,
            readingName = definition.displayName,
            readingGuidance = definition.promptGuidance,
            cards = cards,
            previousReadings = previousReadings,
            isForAnotherPerson = isForAnotherPerson,
            analysis = analysis,
        )

        val parsed = try {
            Json.decodeFromString<LLMTarotRead>(sanitizeJson(read))
        } catch (e: Exception) {
            throw IllegalStateException("Failed to parse LLM response: $read", e)
        }

        // 4) Add the createdAt date and the original question
        val llmTarotRead = parsed.copy(
            createdAt = Timestamp.now(),
            question = question,
        )

        // 5) Save the result in firebase and consume the credit token that covered it
        tarotReadingRepository.addReading(
            userId = userId,
            readingId = readingId.toString(),
            creditToken = creditToken,
            reading = llmTarotRead,
        )
    }

    private fun sanitizeJson(rawResponse: String): String {
        return rawResponse
            .trim()
            // Removes markdown blocks if they exist
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}