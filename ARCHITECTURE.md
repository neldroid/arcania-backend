# Architecture

A walkthrough of the layers, the dependency direction, and how the AI layer plugs in. Companion to the [README](./README.md).

---

## 1. Layers and dependency direction

```
                   ┌───────────────────────────────────────┐
                   │              api/                      │   ← Presentation
                   │   Ktor routes, plugins, auth, BFF      │
                   └──────────────────┬─────────────────────┘
                                      │ depends on
                   ┌──────────────────▼─────────────────────┐
                   │              domain/                   │   ← Pure business rules
                   │                                         │
                   │   usecase/  ── orchestration            │
                   │   repository/  ── INTERFACES            │
                   │   tarot/   ── aggregates                │
                   │                                         │
                   │   ZERO infrastructure imports           │
                   └─────┬─────────────────────────────┬────┘
                         │ implemented by              │ uses
       ┌─────────────────▼────────────┐       ┌────────▼──────────────┐
       │          data/                │       │       agent/           │
       │   Firestore implementations   │       │   LLM layer            │
       │   of domain repositories      │       │   (Koog + Gemini)      │
       └───────────────────────────────┘       └────────────────────────┘
                            ▲
                            │ wires
                   ┌────────┴────────┐
                   │     common/      │
                   │  config + DI     │
                   └─────────────────┘
```

**The rule:** arrows point inward. `domain/` has no `import io.ktor.*`, no `import com.google.cloud.*`, no `import ai.koog.*`. You can compile `domain/` against a hypothetical `data-postgres/` or `agent-anthropic/` with zero changes.

Verify with `grep`:

```bash
# Should print nothing:
grep -rE "import (io\.ktor|com\.google\.cloud|com\.google\.firebase|ai\.koog|com\.stripe)" src/main/kotlin/domain/
```

---

## 2. The `agent/` layer

The AI layer is its own concentric ring with three sub-packages:

### `agent/llm/`

The **provider-agnostic contract**.

- `LLMProvider` — `suspend fun complete(LLMRequest): LLMResponse`.
- `LLMRequest` — `systemPrompt`, `userMessage`, `TraceContext(userId, correlationId, feature)`.
- `LLMResponse` — `content`, `usage: TokenUsage`, `modelId`.
- `GeminiLLMProvider` — the **only file in the entire repo** that imports `ai.koog.*`. Wraps Koog's `AIAgent`, registers `onLLMCallStarting`/`onLLMCallCompleted` event handlers, captures token usage, applies `withTimeout`, throws `LLMTimeoutException` on overrun.

To add a second provider (Anthropic, OpenAI, vLLM, Bedrock):

```kotlin
class AnthropicLLMProvider(apiKey: String) : LLMProvider {
    override suspend fun complete(request: LLMRequest): LLMResponse = ...
}
```

Then change one line in `AppModule.kt`. Zero touches elsewhere.

### `agent/prompts/`

Prompts are **data, not code**. Each file exports:

- `VERSION` — bump for A/B testing or change tracking.
- `systemPrompt(...)` — returns the system prompt string. May be parameterized (e.g. tarot's `isForAnotherPerson`).
- `contextBlock(...)` — returns the user message, built from typed domain inputs (`TarotCard`, themes, emotions, summarized history).

Moving prompts to a database or remote config store (LaunchDarkly, Firestore, your own admin panel) means replacing the `object` with a `class PromptProvider` and injecting it. The use cases don't change.

### `agent/parsing/`

`LLMResponseParser.parse<T>(raw, serializer)`:

1. Trim.
2. Strip ` ```json ` / ` ``` ` fences.
3. Decode via `kotlinx.serialization`.
4. Throw `LLMResponseParseException(rawPayload, cause)` on failure — the raw payload is attached for diagnostics.

This is the only place that knows about Gemini's markdown habit. If Anthropic returns clean JSON, the parser still works — sanitize is idempotent.

---

## 3. The `domain/` layer

### `domain/repository/` — interfaces only

```kotlin
interface TarotReadingRepository {
    suspend fun addReading(userId: String, readingId: String, readingType: String, reading: LLMTarotRead)
    suspend fun getLastReadingSummaries(userId: String, limit: Int = 3): List<String>
    suspend fun markFailed(userId: String, readingId: String, reason: String)
}
```

The domain owns the contract. Firestore implements it (`data/firebase/FirestoreTarotReadingRepository`). A Postgres or in-memory implementation would live under a different package, wired by Koin.

Note the **semantic methods** — `addReading`, `markFailed`, `getLastReadingSummaries`. Not a generic `findAll` / `delete` CRUD. The repository surface is exactly what the use case needs, nothing more (Interface Segregation Principle).

### `domain/usecase/` — orchestration

Each use case is:

- A constructor with **interfaces only** (`LLMProvider`, `LLMResponseParser`, repositories).
- A nested `Command` data class (typed input, no primitive obsession).
- One `suspend fun execute(cmd: Command)` that does:
  1. Authorization / token check.
  2. Read context (history summaries).
  3. Build prompt → LLM → parse.
  4. Persist or mark failed.

Use cases are the only file allowed to throw domain exceptions:

- `InsufficientReadingTokensException`
- `InsufficientDreamTokensException`
- `LLMTimeoutException` (rethrown)
- `LLMResponseParseException` (rethrown after marking failed)

### `domain/tarot/`

Pure aggregates. `TarotCard`, the deck, the shuffler. No frameworks.

---

## 4. The `data/` layer

`data/firebase/Firestore*Repository.kt` implements the domain repository interfaces.

Two things to highlight:

- **Token consumption is atomic inside the repository.** `FirestoreTarotReadingRepository.addReading` writes the reading doc *and* removes the consumed token from the user's `tarot.readings` array in the same logical step. The use case doesn't deal with token accounting — that's a persistence concern.
- **Stripe idempotency uses `Firestore.document.create()`**, which throws if the document already exists. This makes dedup atomic without a transaction: only one of N concurrent webhook deliveries for the same `event.id` can win.

---

## 5. The `api/` layer

### Routes are thin

A typical route:

```kotlin
get {
    val request = call.receive<ReadTarotRequest>()
    val readingId = UUID.randomUUID()

    call.respond(HttpStatusCode.Accepted, ReadTarotResponse(SUCCESS, readingId.toString()))

    call.application.launch {
        try {
            useCase.execute(CreateTarotReadingUseCase.Command(...))
        } catch (e: Exception) {
            log.error(e) { "tarot.background.failed readingId=$readingId" }
        }
    }
}
```

Routes do four things and nothing more:

1. Deserialize the request.
2. Generate an ID.
3. Respond `202 Accepted` immediately.
4. Launch the use case in the background.

No business logic, no parsing, no DB calls. If the route grew beyond this, that would be a smell.

### Cross-cutting concerns

- `App.kt` — Koin install, `RateLimit`, `RequestValidation`, `StatusPages` (validation / deserialization / fallback).
- `api/ApiKeyAuth.kt` — a `createRouteScopedPlugin` that reads `AppConfig` from the Koin runtime and validates `X-Api-Key`.
- `api/plugins/JSONSerialization.kt` — `kotlinx.serialization` with custom `UUID` and Firestore `Timestamp` serializers.

---

## 6. The `common/` layer

### `common/config/AppConfig`

The **only** place in the codebase that reads environment variables. Validation happens at startup via `AppConfig.fromEnv()`. A missing variable fails the process immediately with a precise message — not silently at the first call site that needs it.

```kotlin
data class AppConfig(
    val geminiApiKey: String,
    val firebaseServiceAccount: String,
    val internalApiKey: String,
    val stripeWebhookSecret: String,
    val makeWebhookSecret: String,
    val internalBaseUrl: String,
    val llmTimeoutSeconds: Long,
)
```

### `common/di/AppModule.kt`

Koin wires the layers. Every external collaborator is a `single<Interface> { Implementation(get(), ...) }`, so the **interface** is the visible type in the container. Consumers always inject interfaces.

The most important single binding in the whole file:

```kotlin
single<LLMProvider> {
    val cfg = get<AppConfig>()
    GeminiLLMProvider(apiKey = cfg.geminiApiKey, timeout = cfg.llmTimeoutSeconds.seconds)
}
```

That line is the **entire** coupling between the domain and Gemini. Replacing it is the entire migration to a new provider.

---

## 7. End-to-end: a tarot reading, top to bottom

1. `POST /tarot/reading` hits `BffRoutes`. Rate limited per client IP. Internal proxy call (with `X-Api-Key`) to `POST /read-cards`.
2. `TarotRoutes`/`/read-cards` validates the API key, generates `readingId`, responds `202`, launches the use case.
3. `CreateTarotReadingUseCase.execute(cmd)`:
   - Calls `users.findUser(...)` — token check.
   - Calls `readings.getLastReadingSummaries(...)` — condensed history.
   - Calls `TarotCardHelper.getCards(quantity)` — deck shuffle.
   - Calls `llm.complete(LLMRequest(systemPrompt = TarotPrompts.systemPrompt(...), userMessage = TarotPrompts.contextBlock(...)))`.
4. `GeminiLLMProvider.complete(...)`:
   - Builds Koog `AIAgent` with the system prompt.
   - Registers event handlers → structured logs + captured token usage.
   - `withTimeout(90s) { agent.run(userMessage) }`.
   - Returns `LLMResponse(content, usage, modelId)`.
5. `parser.parse(response.content, LLMTarotRead.serializer())` — strip fences, decode.
6. `readings.addReading(...)` — persist reading + atomically decrement the user's token.
7. Client (subscribed to `users/{uid}/readings/{readingId}` via Firebase SDK) receives the document reactively.

**On failure** (timeout, parse error): `readings.markFailed(userId, readingId, reason)` writes a `{ status: FAILED, failureReason, failedAt }` document at the same path. The client's listener fires, the UI shows an error, polling ends.

---

## 8. SOLID, mapped to files

| Principle | Where you see it |
| --- | --- |
| **Single Responsibility** | `LLMProvider` calls the model; `LLMResponseParser` parses; `TarotPrompts` builds prompts; `TarotReadingRepository` persists; `CreateTarotReadingUseCase` orchestrates. None of them know anything about the others' jobs. |
| **Open/Closed** | Add a new LLM provider, a new repository backend, or a new prompt variant — none require modifying existing code, only adding a new class and rebinding in Koin. |
| **Liskov Substitution** | All repositories are interfaces with semantic methods (no generic CRUD leaking through). Any implementation satisfies the contract. |
| **Interface Segregation** | `UserRepository` exposes `grantTarotReading` / `grantDreamInterpretation` / `consumeReikiAppointment` — not a generic `update(Map<String, Any>)`. The webhook only sees what it needs. |
| **Dependency Inversion** | Use cases depend on `LLMProvider` and `*Repository` interfaces in `domain/`. Implementations are injected at the composition root. The domain is independently compilable. |

---

## 9. What would extend cleanly tomorrow

| Extension | What changes | What does NOT change |
| --- | --- | --- |
| **Add Anthropic / OpenAI / Bedrock provider** | New `AnthropicLLMProvider : LLMProvider`. One Koin binding. | Use cases, prompts, parser, routes, repositories. |
| **RAG (vector lookup of past readings or tarot lore)** | New `KnowledgeRetriever` interface + impl. Use case calls it before building the prompt; appends retrieved snippets to `contextBlock`. | LLM provider, parser, repositories. |
| **Streaming responses** | Extend `LLMProvider` with `suspend fun stream(LLMRequest): Flow<String>`. Add a streaming use case. SSE/WebSocket route. | The non-streaming path keeps working. |
| **Tool use / function calling** | `LLMProvider.complete` overload that takes `tools: List<ToolDefinition>`. Use case maps tools to domain calls. | Prompts, parser, repositories. |
| **Move prompts to a remote store** | Replace prompt `object`s with a `PromptProvider` class + Koin binding. | Use cases (they call the same interface), LLM provider, repositories. |
| **Postgres instead of Firestore** | New `data-postgres/` package with implementations of the same repository interfaces. | Domain, use cases, agent layer, routes. |

---

## 10. What is intentionally NOT here

- **Tests.** Not in scope for this refactor; the seams are now in place so they're trivial to add (mock `LLMProvider`, mock repositories).
- **Distributed tracing (OpenTelemetry).** The `TraceContext` plumbing is in place; an OTel exporter would attach in `GeminiLLMProvider` and the logging config.
- **Multi-tenant prompt versioning.** Single version per prompt today; the `VERSION` constant marks the seam.
