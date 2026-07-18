package common.di

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import data.firebase.DreamInterpretationRepository
import data.firebase.TarotReadingRepository
import data.firebase.UserRepository
import domain.dream.DreamService
import domain.tarot.TarotService
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import java.io.ByteArrayInputStream
import java.io.File

private fun loadFirebaseCredentials(): GoogleCredentials {
    val value = System.getenv("FIREBASE_SERVICE_ACCOUNT")
        ?: error("FIREBASE_SERVICE_ACCOUNT environment variable is not set")

    // Accept either the raw service-account JSON (how prod/Render supplies it)
    // or a path to a JSON file (convenient locally, where pasting multi-line
    // JSON into a run config mangles the private_key newlines).
    val stream = if (value.trimStart().startsWith("{")) {
        ByteArrayInputStream(value.toByteArray())
    } else {
        File(value).inputStream()
    }
    return GoogleCredentials.fromStream(stream)
}

val appModule = module {
    single<FirebaseApp> {
        if (FirebaseApp.getApps().isNotEmpty()) {
            FirebaseApp.getInstance()
        } else {
            val credentials = loadFirebaseCredentials()
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build()
            FirebaseApp.initializeApp(options)
        }
    }

    // Inject specific Firebase services you need
    single<Firestore> {
        get<FirebaseApp>() // ensures FirebaseApp is initialized first
        FirestoreClient.getFirestore()
    }

    single {
        HttpClient(CIO){
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000  // 2 minutes
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 120_000
            }
        }
    }

    /* Add repositories injection
    Ex.: single<Repository> {RepositoryImpl()}
    */
    single { TarotReadingRepository(get<Firestore>()) }
    single { DreamInterpretationRepository(get<Firestore>()) }
    single { UserRepository(get<Firestore>()) }
    /* Add use cases injection
    Ex.: single { UseCase() }
     */
    single { TarotService(get<TarotReadingRepository>(), get<UserRepository>()) }
    single { DreamService(get<DreamInterpretationRepository>(), get<UserRepository>()) }

    /* Add Notifiers
    Ex.: single { Notifier() }
     */
}