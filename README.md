# Magestika · Tarot Backend

> An **AI-native** spiritual guidance backend. LLM agents generate tarot readings and dream interpretations, orchestrated with **Koog Agents** on top of **Google Gemini 2.5 Flash**, behind a **Kotlin / Ktor** API designed with **Clean Architecture**, dependency inversion, and a swappable LLM provider.

---

## TL;DR — what to look at first

If you only have two minutes, open these files in this order:

1. `agent/llm/LLMProvider.kt` — the provider-agnostic LLM contract.
2. `agent/llm/GeminiLLMProvider.kt` — the only file in the project that imports anything from Koog.
3. `agent/prompts/TarotPrompts.kt` — the tarot agent's persona, constraints, and JSON contract (prompts as data).
4. `domain/usecase/CreateTarotReadingUseCase.kt` — the orchestration. It has zero infrastructure imports.
5. `common/di/AppModule.kt` — Koin wiring: swap `GeminiLLMProvider` for an Anthropic implementation here, change nothing else.

---

## 1. What this service does

| Domain | What the AI does | Output |
| --- | --- | --- |
| **Tarot reading** | Interprets a 1- or 3-card spread against the user's question, emotional state, themes, and recent reading history. Supports a *third-person mode* for readings done for someone else. | Typed JSON: `opening` · `cards[]` (per-card analysis) · `synthesis` · `guidance` · `summary` |
| **Dream interpretation** | Decomposes a dream into symbols and Jungian archetypes (Shadow, Anima/Animus, Self), synthesizes the emotional message, and offers reflective guidance. | Typed JSON: `overview` · `symbols[]` · `emotional` · `guidance` · `summary` |
| **Reiki appointments** | Commerce + booking lifecycle (Stripe-backed). | Booking state in Firestore |

Payments flow through **Stripe Checkout + webhooks**. The user wallet (reading tokens, dream tokens, reiki appointments) is updated when `checkout.session.completed` fires, **deduplicated by Stripe `event.id`** so retries never double-credit.

---

## 2. AI architecture — the heart of the system

The differentiator is the AI layer. It's built so the **domain doesn't know it's using Gemini** — or Koog, or any specific framework.

```
┌─────────────────────────────────────────────────────────────────────┐
│                       domain/usecase                                 │
│  CreateTarotReadingUseCase · InterpretDreamUseCase                   │
│  depend ONLY on:                                                     │
│    · LLMProvider (interface)                                         │
│    · LLMResponseParser                                               │
│    · *Repository (interfaces)                                        │
└──────────────────┬──────────────────────────────────────────────────┘
                   │ uses
┌──────────────────▼──────────────────────────────────────────────────┐
│                       agent/  (the AI layer)                         │
│                                                                       │
│   llm/LLMProvider.kt        ◀── provider-agnostic contract           │
│       └── LLMRequest / LLMResponse / TokenUsage / TraceContext       │
│                                                                       │
│   llm/GeminiLLMProvider.kt  ◀── ONLY file in repo importing Koog     │
│       · Koog AIAgent + simpleGoogleAIExecutor                        │
│       · withTimeout() around the call                                │
│       · onLLMCallStarting / onLLMCallCompleted → structured logs     │
│       · captures input/output/total tokens into LLMResponse          │
│                                                                       │
│   prompts/TarotPrompts.kt   ◀── system prompt + context builder      │
│   prompts/DreamPrompts.kt        as data (versioned, A/B-testable)   │
│                                                                       │
│   parsing/LLMResponseParser.kt   ◀── strips ``` fences, decodes      │
│                                       into kotlinx.serialization     │
└─────────────────────────────────────────────────────────────────────┘
```

### 2.1 The provider abstraction (the win)

`agent/llm/LLMProvider.kt`:

```kotlin
interface LLMProvider {
    suspend fun complete(request: LLMRequest): LLMResponse
}
```

That's the entire dependency the domain has on the AI world. `LLMRequest` carries `systemPrompt`, `userMessage`, and a `TraceContext(userId, correlationId, feature)`. `LLMResponse` carries `content`, `usage: TokenUsage`, and `modelId`.

Swapping Gemini for Anthropic is **one Koin binding**:

```kotlin
// AppModule.kt
single<LLMProvider> { GeminiLLMProvider(cfg.geminiApiKey, timeout = ...) }
// becomes
single<LLMProvider> { AnthropicLLMProvider(cfg.anthropicApiKey, ...) }
```

No use case, no route, no repository changes. That's Dependency Inversion in action.

### 2.2 Each agent is a five-part contract

Every prompt (in `agent/prompts/`) follows the same skeleton:

1. **Role / Persona** — precise professional identity (`Professional Tarot Reader AI`, `Dream Interpreter AI with deep knowledge of Jungian archetypes`).
2. **Language & tone contract** — `Español Neutro` (accessible Spain + LATAM), explicit pronoun policy, conditional **third-person mode** when the reading is for someone other than the querent.
3. **Hard constraints** — no fatalism, no medical/legal/financial advice, no fortune-telling. Outputs are framed as *possibilities* and *reflections*, never absolutes.
4. **Numbered interpretation logic** — a deterministic reasoning protocol (analyze → synthesize → personalize → guide).
5. **Strict output contract** — JSON only, starts with `{`, ends with `}`, mirrored by a Kotlin `@Serializable` data class.

### 2.3 Context engineering — small, condensed, summarized

The user message sent to the model is intentionally compact:

```text
### USER_DATA
Name: María
Query: "¿Qué necesito ver con claridad en esta etapa?"
Context: [Themes: trabajo, transición | Emotions: incertidumbre, esperanza]
History: ciclo de cierre y nuevo comienzo | claridad emocional pendiente | momento de pausa

### SPREAD_DATA
- CARD: ID:9 | NAME: The Hermit | POS: Past | ORIENT: UPRIGHT
  ATTR: MAJOR, null | KEYS: introspection, solitude, wisdom, guidance, reflection
- CARD: ID:13 | NAME: Death | POS: Present | ORIENT: REVERSED
  ATTR: MAJOR, null | KEYS: resistance, fear of change, stagnation, clinging, delay
- CARD: ID:19 | NAME: The Sun | POS: Future | ORIENT: UPRIGHT
  ATTR: MAJOR, null | KEYS: success, joy, clarity, positivity, vitality
```

Three design choices to highlight:

- **Single-line per card** is more token-efficient and easier for the model to scan than nested JSON.
- **Only summaries** of past readings are reinjected as `History` — never full transcripts. Token cost stays bounded as a user's history grows. The summaries are produced by the model itself in the previous reading's `summary` field and stored in Firestore.
- The `themes` and `emotions` arrays come from the client UI, kept as short tag lists rather than free text.

### 2.4 Observability — every LLM call is traced

`GeminiLLMProvider` registers Koog event handlers and emits **structured SLF4J logs** with userId, correlationId, feature, model, and token counts:

```
llm.call.start   model=gemini-2.5-flash feature=tarot userId=abc correlationId=…
llm.call.success model=gemini-2.5-flash feature=tarot userId=abc tokens.in=812 tokens.out=437 tokens.total=1249
```

Token counts also flow back into `LLMResponse.usage`, so use cases log per-feature totals. This is the foundation for cost dashboards, per-user usage caps, and prompt A/B testing.

### 2.5 Resilience — timeout + failure state

The LLM call is wrapped in `withTimeout(cfg.llmTimeoutSeconds.seconds)`. On timeout or parse failure, the use case writes a **failure marker** to Firestore at the same `readingId` the client is polling, so the client stops polling and the UX can show a clear error — instead of an indefinite spinner.

```kotlin
val response = try {
    llm.complete(request)
} catch (e: LLMTimeoutException) {
    readings.markFailed(cmd.userId, cmd.readingId.toString(), "llm_timeout")
    throw e
}
```

### 2.6 Typed responses — JSON sanitized once, decoded once

`LLMResponseParser` strips markdown fences (`\`\`\`json` / `\`\`\``) and decodes into a typed `@Serializable` model. One failure point, one clear exception (`LLMResponseParseException` carrying the raw payload).

---

## 3. End-to-end request flow

```
   Client                Route              UseCase              LLMProvider          Firestore
     │                     │                   │                     │                     │
     │── POST reading ────▶│                   │                     │                     │
     │◀─ 202 + readingId ──│                   │                     │                     │
     │                     │── launch{} ──────▶│                     │                     │
     │                     │                   │── check wallet ─────┼────────────────────▶│
     │                     │                   │◀── tokens OK ───────┼─────────────────────│
     │                     │                   │── fetch summaries ──┼────────────────────▶│
     │                     │                   │◀── last 3 ──────────┼─────────────────────│
     │                     │                   │── deal cards ───────│                     │
     │                     │                   │── complete() ──────▶│ Gemini 2.5 Flash    │
     │                     │                   │                     │ (withTimeout 90s)   │
     │                     │                   │◀── LLMResponse ─────│                     │
     │                     │                   │── parse JSON ───────│                     │
     │                     │                   │── persist reading ──┼────────────────────▶│
     │── poll readingId ──▶│                   │                     │                     │
     │◀── reading ─────────│                   │                     │                     │
```

Endpoint returns `202 Accepted` instantly with a `readingId`. The client subscribes to `users/{uid}/readings/{readingId}` via the Firebase SDK and gets the result reactively when the use case finishes writing it — or a `FAILED` document on timeout/parse error.

---

## 4. Project layout

```
src/main/kotlin
├── App.kt                                  # Ktor bootstrap: rate limit, validation, status pages
├── api/
│   ├── ApiKeyAuth.kt                       # X-Api-Key plugin, reads from AppConfig
│   ├── plugins/                            # Routing, ContentNegotiation, kotlinx JSON
│   └── routes/
│       ├── TarotRoutes.kt                  # POST /read-cards
│       ├── DreamRoutes.kt                  # POST /interpret-dream
│       ├── BffRoutes.kt                    # Public, rate-limited proxy to internal routes
│       ├── StripeRoutes.kt                 # POST /stripe/webhook  (signature + idempotency)
│       └── ReikiRoutes.kt                  # POST /reiki/confirm
│
├── agent/                                  # AI layer (only place that knows about LLMs)
│   ├── llm/
│   │   ├── LLMProvider.kt                  # interface + request/response/usage types
│   │   └── GeminiLLMProvider.kt            # Koog + Gemini, sole owner of `ai.koog.*` imports
│   ├── prompts/
│   │   ├── TarotPrompts.kt                 # system prompt + context builder
│   │   └── DreamPrompts.kt
│   └── parsing/
│       └── LLMResponseParser.kt            # typed JSON parser
│
├── domain/                                 # Business rules. No Ktor / Koog / Firestore imports.
│   ├── tarot/
│   │   ├── TarotCard.kt                    # Aggregate
│   │   └── TarotCardHelper.kt              # Deck + spread positions
│   ├── repository/                         # Repository INTERFACES (DIP)
│   │   ├── TarotReadingRepository.kt
│   │   ├── DreamInterpretationRepository.kt
│   │   ├── UserRepository.kt
│   │   └── StripeEventRepository.kt
│   └── usecase/                            # Application services
│       ├── CreateTarotReadingUseCase.kt
│       ├── InterpretDreamUseCase.kt
│       └── ProcessStripeEventUseCase.kt
│
├── data/firebase/                          # Repository IMPLEMENTATIONS
│   ├── FirestoreTarotReadingRepository.kt
│   ├── FirestoreDreamInterpretationRepository.kt
│   ├── FirestoreUserRepository.kt
│   └── FirestoreStripeEventRepository.kt   # Stripe idempotency (create-only)
│
└── common/
    ├── config/AppConfig.kt                 # All env vars, validated at startup
    ├── di/AppModule.kt                     # Koin wiring
    └── model/                              # User, LLMTarotRead, LLMDreamInterpretation
```

---

## 5. Tech stack

| Concern | Choice |
| --- | --- |
| Language | **Kotlin 2.3** on JVM 21 |
| HTTP server | **Ktor 3.2** + Netty |
| AI framework | **Koog Agents 0.6.2** |
| LLM | **Google Gemini 2.5 Flash** |
| Persistence | **Cloud Firestore** (Firebase Admin SDK 9.8) |
| DI | **Koin 4.1** |
| Serialization | **kotlinx.serialization 1.9** |
| Logging | **SLF4J via kotlin-logging 7.0** + Logback |
| Payments | **Stripe Java SDK 29.1** |
| Concurrency | Kotlin Coroutines 1.10 |
| Build | Gradle + Shadow (fat JAR) |
| Runtime | Docker (JDK 21 → JRE 21) |

---

## 6. Cross-cutting concerns

| Concern | Where | Detail |
| --- | --- | --- |
| **API key auth** | `api/ApiKeyAuth.kt` | `X-Api-Key` checked against `AppConfig.internalApiKey`. |
| **Rate limiting** | `App.kt` | Public BFF capped at **20 req/h per client IP** (`X-Forwarded-For` aware). |
| **Request validation** | `App.kt` | Ktor `RequestValidation` rejects malformed `cardsQuantity` / empty `question` / `dreamDescription`. |
| **Webhook signature** | `StripeRoutes.kt` | `Webhook.constructEvent(payload, sig, secret)` before any state mutation. |
| **Webhook idempotency** | `FirestoreStripeEventRepository.kt` | `document.create(eventId)` (create-only) — duplicate retries throw and are returned as already-processed. |
| **LLM timeout** | `GeminiLLMProvider.kt` | `withTimeout(cfg.llmTimeoutSeconds.seconds)`. Throws `LLMTimeoutException`. |
| **Failure state** | Use cases | Persist `{ status: FAILED, failureReason }` so polling clients get a terminal state. |
| **Error pipeline** | `App.kt` `StatusPages` | Validation / deserialization / unknown errors → consistent HTTP responses. |

---

## 7. Endpoints

| Method | Path | Surface | Purpose |
| --- | --- | --- | --- |
| `POST` | `/tarot/reading` | Public BFF (rate-limited) | User-facing tarot reading entry point |
| `POST` | `/dream/interpretation` | Public BFF (rate-limited) | User-facing dream interpretation entry point |
| `POST` | `/read-cards` | Internal (X-Api-Key) | Direct tarot reading endpoint |
| `POST` | `/interpret-dream` | Internal (X-Api-Key) | Direct dream interpretation endpoint |
| `POST` | `/stripe/webhook` | Stripe (signed) | Provisions tokens on `checkout.session.completed` (idempotent) |
| `POST` | `/reiki/confirm` | Make.com (x-make-secret) | Confirms a reiki appointment by email |

---

## 8. Local development

```bash
# Required env vars (validated at startup by AppConfig)
export GEMINI_API_KEY=...
export FIREBASE_SERVICE_ACCOUNT='{ ...service account JSON... }'
export TAROT_API_KEY=...
export STRIPE_WEBHOOK_SECRET=whsec_...
export MAKE_WEBHOOK_SECRET=...

# Optional
export INTERNAL_BASE_URL=http://localhost:8080   # default
export LLM_TIMEOUT_SECONDS=90                    # default

./gradlew run                   # boots Ktor on port 8080
./gradlew shadowJar             # fat JAR for Docker
docker build -t magestika-backend .
```

---

## 9. Engineering principles applied

- **Clean Architecture with strict dependency direction.** Domain knows nothing about Ktor, Firestore, Stripe, or Koog. `agent/` (the AI layer) and `data/` (persistence) depend on domain interfaces, never the other way around.
- **Dependency Inversion at every boundary.** `LLMProvider`, `TarotReadingRepository`, `DreamInterpretationRepository`, `UserRepository`, `StripeEventRepository` are all interfaces owned by the domain. Implementations live in outer layers and are wired in `AppModule.kt`.
- **Prompts as data, not code.** `agent/prompts/` is the single place to evolve the persona, constraints, output schema, or tone.
- **Single Responsibility everywhere.** Use cases orchestrate, providers call the LLM, parsers parse, repositories persist, routes speak HTTP.
- **Async by default.** AI work runs in `application.launch{}`. The HTTP request never waits.
- **Token economy.** Context is condensed; history is summarized; every call is traced with token counts.
- **Safety contracts in the prompt.** Non-deterministic phrasing, professional boundaries, JSON-only output baked into the system prompt and enforced by typed deserialization.
- **Centralized config.** `AppConfig` is the only place that touches `System.getenv` and validates everything at startup.

---

## 10. Where to go next

See [`ARCHITECTURE.md`](./ARCHITECTURE.md) for the deeper architectural walkthrough — layer-by-layer responsibilities, dependency direction, and the path to add a second LLM provider, RAG, tool use, or streaming without breaking the existing seams.
