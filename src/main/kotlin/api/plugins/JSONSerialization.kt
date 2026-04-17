package api.plugins

import com.google.cloud.Timestamp
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.*

val appJson = Json {
    serializersModule = SerializersModule {
        contextual(UUID::class, UUIDSerializer)
        contextual(Timestamp::class, TimeStampSerializer)
    }
    encodeDefaults = true
    ignoreUnknownKeys = true
}

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }
}

object TimeStampSerializer : KSerializer<Timestamp> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Timestamp", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Timestamp) {
        encoder.encodeString(value.toString()) // Saves as ISO-8601 string
    }

    override fun deserialize(decoder: Decoder): Timestamp {
        return Timestamp.parseTimestamp(decoder.decodeString())
    }
}