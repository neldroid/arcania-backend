package agent.parsing

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * Typed parser for raw LLM string output.
 *
 * Handles the two real-world quirks Gemini still produces despite a strict
 * JSON system prompt: markdown code fences and leading/trailing whitespace.
 * Returns a typed [T] or throws [LLMResponseParseException] with the raw
 * payload attached for diagnostics.
 */
class LLMResponseParser(
    private val json: Json = DEFAULT_JSON,
) {
    fun <T> parse(raw: String, serializer: KSerializer<T>): T {
        val sanitized = sanitize(raw)
        return try {
            json.decodeFromString(serializer, sanitized)
        } catch (e: Exception) {
            throw LLMResponseParseException(raw, e)
        }
    }

    private fun sanitize(raw: String): String =
        raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

    companion object {
        private val DEFAULT_JSON = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }
    }
}

class LLMResponseParseException(
    val rawPayload: String,
    cause: Throwable,
) : RuntimeException("Failed to parse LLM response: $rawPayload", cause)
