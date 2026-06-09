package common.di

import agent.llm.GeminiLLMProvider
import agent.llm.LLMProvider
import agent.parsing.LLMResponseParser
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import common.config.AppConfig
import data.firebase.FirestoreDreamInterpretationRepository
import data.firebase.FirestoreStripeEventRepository
import data.firebase.FirestoreTarotReadingRepository
import data.firebase.FirestoreUserRepository
import domain.repository.DreamInterpretationRepository
import domain.repository.StripeEventRepository
import domain.repository.TarotReadingRepository
import domain.repository.UserRepository
import domain.usecase.CreateTarotReadingUseCase
import domain.usecase.InterpretDreamUseCase
import domain.usecase.ProcessStripeEventUseCase
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.io.ByteArrayInputStream
import kotlin.time.Duration.Companion.seconds

val appModule = module {

    single { AppConfig.fromEnv() }

    single<FirebaseApp> {
        if (FirebaseApp.getApps().isNotEmpty()) {
            FirebaseApp.getInstance()
        } else {
            val cfg = get<AppConfig>()
            val credentials = GoogleCredentials.fromStream(
                ByteArrayInputStream(cfg.firebaseServiceAccount.toByteArray())
            )
            FirebaseApp.initializeApp(
                FirebaseOptions.builder().setCredentials(credentials).build()
            )
        }
    }

    single<Firestore> {
        get<FirebaseApp>()
        FirestoreClient.getFirestore()
    }

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 120_000
            }
        }
    }

    // LLM provider — swap GeminiLLMProvider for AnthropicLLMProvider here, nothing else changes.
    single<LLMProvider> {
        val cfg = get<AppConfig>()
        GeminiLLMProvider(
            apiKey = cfg.geminiApiKey,
            timeout = cfg.llmTimeoutSeconds.seconds,
        )
    }

    single { LLMResponseParser() }

    // Repositories
    single<TarotReadingRepository> { FirestoreTarotReadingRepository(get()) }
    single<DreamInterpretationRepository> { FirestoreDreamInterpretationRepository(get()) }
    single<UserRepository> { FirestoreUserRepository(get()) }
    single<StripeEventRepository> { FirestoreStripeEventRepository(get()) }

    // Use cases
    single { CreateTarotReadingUseCase(get(), get(), get(), get()) }
    single { InterpretDreamUseCase(get(), get(), get(), get()) }
    single { ProcessStripeEventUseCase(get(), get()) }
}
