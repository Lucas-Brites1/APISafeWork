package com.server.Utils

import io.github.cdimascio.dotenv.dotenv
import io.ktor.util.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.encoding.Encoder
import org.bson.types.ObjectId
import at.favre.lib.crypto.bcrypt.BCrypt

object Utils {
    private val dotenv = dotenv()

    fun JSONResponse(vararg pairs: Pair<String, Any>): Map<String, Any> {
        return mapOf(*pairs)
    }

    fun getEnv(key: String): String? {
        return dotenv[key]
    }

    @kotlinx.serialization.Serializable
    data class LoginInfos(
        val userId: String,
        val username: String,
        val email: String
    )

    @kotlinx.serialization.Serializable
    data class LoginResponse(
        val mensagem: String,
        val infos: LoginInfos
    )
}

object CustomSerializer : KSerializer<ObjectId> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ObjectId", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ObjectId) {
        encoder.encodeString(value.toHexString())
    }

    override fun deserialize(decoder: Decoder): ObjectId  {
        return ObjectId(decoder.decodeString())
    }
}

object Hasher {
    private const val COST = 12

    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(COST, password.toCharArray())
    }

    fun verify(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }
}

