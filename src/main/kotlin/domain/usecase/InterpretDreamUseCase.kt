package domain.usecase

import agent.llm.LLMProvider
import agent.llm.LLMRequest
import agent.llm.LLMTimeoutException
import agent.llm.TraceContext
import agent.parsing.LLMResponseParseException
import agent.parsing.LLMResponseParser
import agent.prompts.DreamPrompts
import com.google.cloud.Timestamp
import common.model.dream.LLMDreamInterpretation
import domain.repository.DreamInterpretationRepository
import domain.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.UUID

private val log = KotlinLogging.logger {}

class InterpretDreamUseCase(
    private val llm: LLMProvider,
    private val parser: LLMResponseParser,
    private val dreams: DreamInterpretationRepository,
    private val users: UserRepository,
) {

    data class Command(
        val interpretationId: UUID,
        val userId: String,
        val userName: String,
        val dreamDescription: String,
        val themes: List<String>,
        val emotions: List<String>,
    )

    suspend fun execute(cmd: Command) {
        val available = users.findUser(cmd.userId)?.dream?.readings?.size ?: 0
        if (available == 0) {
            throw InsufficientDreamTokensException(cmd.userId)
        }

        val previousSummaries = dreams.getLastInterpretationSummaries(cmd.userId)

        val response = try {
            llm.complete(
                LLMRequest(
                    systemPrompt = DreamPrompts.SYSTEM_PROMPT,
                    userMessage = DreamPrompts.contextBlock(
                        userName = cmd.userName,
                        dreamDescription = cmd.dreamDescription,
                        themes = cmd.themes,
                        emotions = cmd.emotions,
                        previousInterpretations = previousSummaries,
                    ),
                    traceContext = TraceContext(
                        userId = cmd.userId,
                        correlationId = cmd.interpretationId.toString(),
                        feature = "dream",
                    ),
                )
            )
        } catch (e: LLMTimeoutException) {
            dreams.markFailed(cmd.userId, cmd.interpretationId.toString(), "llm_timeout")
            throw e
        }

        val parsed = try {
            parser.parse(response.content, LLMDreamInterpretation.serializer())
        } catch (e: LLMResponseParseException) {
            dreams.markFailed(cmd.userId, cmd.interpretationId.toString(), "llm_parse_error")
            log.error(e) { "dream.parse.failed userId=${cmd.userId} interpretationId=${cmd.interpretationId}" }
            throw e
        }

        dreams.addInterpretation(
            userId = cmd.userId,
            interpretationId = cmd.interpretationId.toString(),
            interpretation = parsed.copy(createdAt = Timestamp.now()),
        )

        log.info {
            "dream.interpretation.completed userId=${cmd.userId} " +
                    "interpretationId=${cmd.interpretationId} tokens.total=${response.usage.totalTokens}"
        }
    }
}

class InsufficientDreamTokensException(userId: String) :
    IllegalStateException("User $userId has no dream interpretation tokens left")
