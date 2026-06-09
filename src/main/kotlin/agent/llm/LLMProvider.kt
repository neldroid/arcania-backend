package agent.llm

/**
 * Provider-agnostic contract for invoking a Large Language Model.
 *
 * Domain code depends on this interface, not on Koog, Gemini, or any concrete SDK.
 * Swapping providers (Gemini → Anthropic → OpenAI) is a single Koin binding change.
 */
interface LLMProvider {
    suspend fun complete(request: LLMRequest): LLMResponse
}

data class LLMRequest(
    val systemPrompt: String,
    val userMessage: String,
    val traceContext: TraceContext = TraceContext(),
)

data class LLMResponse(
    val content: String,
    val usage: TokenUsage,
    val modelId: String,
)

data class TokenUsage(
    val inputTokens: Int,
    val outputTokens: Int,
    val totalTokens: Int,
)

data class TraceContext(
    val userId: String? = null,
    val correlationId: String? = null,
    val feature: String? = null,
)
