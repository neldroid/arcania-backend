package agent.llm

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

private val log = KotlinLogging.logger {}

/**
 * Koog-backed implementation targeting Google Gemini.
 *
 * Wraps Koog's [AIAgent] behind [LLMProvider] so the rest of the codebase
 * never imports anything from `ai.koog.*`. Token usage and timing are captured
 * via Koog's event handler and exposed as structured logs + an [LLMResponse].
 */
class GeminiLLMProvider(
    private val apiKey: String,
    private val model: String = GoogleModels.Gemini2_5Flash.id,
    private val timeout: Duration = Duration.parse("90s"),
) : LLMProvider {

    override suspend fun complete(request: LLMRequest): LLMResponse {
        val captured = CapturedUsage()

        val agent = AIAgent(
            promptExecutor = simpleGoogleAIExecutor(apiKey),
            llmModel = GoogleModels.Gemini2_5Flash,
            systemPrompt = request.systemPrompt,
        ) {
            handleEvents {
                onLLMCallStarting { req ->
                    log.info {
                        "llm.call.start model=${req.model.id} " +
                                "feature=${request.traceContext.feature} " +
                                "userId=${request.traceContext.userId} " +
                                "correlationId=${request.traceContext.correlationId}"
                    }
                }
                onLLMCallCompleted { ctx ->
                    ctx.responses.forEach { resp ->
                        captured.input += resp.metaInfo.inputTokensCount ?: 0
                        captured.output += resp.metaInfo.outputTokensCount ?: 0
                        captured.total += resp.metaInfo.totalTokensCount ?: 0
                    }
                    log.info {
                        "llm.call.success model=${ctx.model.id} " +
                                "feature=${request.traceContext.feature} " +
                                "userId=${request.traceContext.userId} " +
                                "tokens.in=${captured.input} " +
                                "tokens.out=${captured.output} " +
                                "tokens.total=${captured.total}"
                    }
                }
            }
        }

        val content = try {
            withTimeout(timeout) { agent.run(request.userMessage) }
        } catch (e: TimeoutCancellationException) {
            log.warn { "llm.call.timeout feature=${request.traceContext.feature} timeout=$timeout" }
            throw LLMTimeoutException("LLM call exceeded $timeout", e)
        }

        return LLMResponse(
            content = content,
            usage = TokenUsage(captured.input, captured.output, captured.total),
            modelId = model,
        )
    }

    private class CapturedUsage(var input: Int = 0, var output: Int = 0, var total: Int = 0)
}

class LLMTimeoutException(message: String, cause: Throwable) : RuntimeException(message, cause)
