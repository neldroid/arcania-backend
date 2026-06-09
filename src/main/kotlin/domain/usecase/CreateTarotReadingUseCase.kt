package domain.usecase

import agent.llm.LLMProvider
import agent.llm.LLMRequest
import agent.llm.LLMTimeoutException
import agent.llm.TraceContext
import agent.parsing.LLMResponseParseException
import agent.parsing.LLMResponseParser
import agent.prompts.TarotPrompts
import com.google.cloud.Timestamp
import common.model.tarot.LLMTarotRead
import domain.repository.TarotReadingRepository
import domain.repository.UserRepository
import domain.tarot.TarotCardHelper
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID

private val log = KotlinLogging.logger {}

/**
 * Orchestrates a tarot reading end-to-end.
 *
 * Responsibilities (only):
 * 1. Verify the user has a reading token of the requested type.
 * 2. Pull condensed history context.
 * 3. Deal cards.
 * 4. Build prompt → invoke LLM → parse response.
 * 5. Persist the reading; consume the token atomically inside the repo.
 *
 * Anything else (HTTP, JSON serialization quirks, provider SDK, Firestore)
 * lives behind an interface. This class compiles without Ktor, Koog, or Firestore.
 */
class CreateTarotReadingUseCase(
    private val llm: LLMProvider,
    private val parser: LLMResponseParser,
    private val readings: TarotReadingRepository,
    private val users: UserRepository,
) {

    data class Command(
        val readingId: UUID,
        val userId: String,
        val userName: String,
        val cardsQuantity: Int,
        val question: String,
        val themes: List<String>,
        val emotions: List<String>,
        val isForAnotherPerson: Boolean = false,
    )

    suspend fun execute(cmd: Command) {
        val readingType = if (cmd.cardsQuantity == 1) "single" else "three"
        val available = users.findUser(cmd.userId)?.tarot?.readings
            ?.count { it == readingType } ?: 0

        if (available == 0) {
            throw InsufficientReadingTokensException(cmd.userId, readingType)
        }

        val previousSummaries =
            if (cmd.isForAnotherPerson) emptyList()
            else readings.getLastReadingSummaries(cmd.userId)

        val cards = TarotCardHelper.getCards(cmd.cardsQuantity)

        val response = try {
            llm.complete(
                LLMRequest(
                    systemPrompt = TarotPrompts.systemPrompt(cmd.isForAnotherPerson),
                    userMessage = TarotPrompts.contextBlock(
                        userName = cmd.userName,
                        question = cmd.question,
                        cards = cards,
                        themes = cmd.themes,
                        emotions = cmd.emotions,
                        previousReadings = previousSummaries,
                        isForAnotherPerson = cmd.isForAnotherPerson,
                    ),
                    traceContext = TraceContext(
                        userId = cmd.userId,
                        correlationId = cmd.readingId.toString(),
                        feature = "tarot",
                    ),
                )
            )
        } catch (e: LLMTimeoutException) {
            readings.markFailed(cmd.userId, cmd.readingId.toString(), "llm_timeout")
            throw e
        }

        val parsed = try {
            parser.parse(response.content, LLMTarotRead.serializer())
        } catch (e: LLMResponseParseException) {
            readings.markFailed(cmd.userId, cmd.readingId.toString(), "llm_parse_error")
            log.error(e) { "tarot.parse.failed userId=${cmd.userId} readingId=${cmd.readingId}" }
            throw e
        }

        readings.addReading(
            userId = cmd.userId,
            readingId = cmd.readingId.toString(),
            readingType = readingType,
            reading = parsed.copy(createdAt = Timestamp.now()),
        )

        log.info {
            "tarot.reading.completed userId=${cmd.userId} readingId=${cmd.readingId} " +
                    "type=$readingType tokens.total=${response.usage.totalTokens}"
        }
    }
}

class InsufficientReadingTokensException(userId: String, readingType: String) :
    IllegalStateException("User $userId has no '$readingType' reading tokens left")
