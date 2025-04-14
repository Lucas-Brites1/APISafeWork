package com.server.Models

import com.server.Utils.CustomSerializer
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import kotlinx.serialization.Contextual

@Serializable
data class UserModel(
    @Serializable(with = CustomSerializer::class)
    val _id: ObjectId? = null,
    val email: String,
    val username: String,
    val password: String,
    val role: Role? = Role.USER
)

enum class Role {
    DEV,
    ADMIN,
    USER
}