package com.server.Models

import com.server.Utils.CustomSerializer
import com.server.Utils.LocalDateTimeSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId
import java.time.LocalDateTime
import java.util.*

@Serializable
data class IssueModel(
    @Serializable(with = CustomSerializer::class)
    val _id: ObjectId,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    val user: IssueUser,
    val level: IssueLevel,
    val status: IssueStatus,
    val mapLocal: LocationModel,
    val title: String,
    val description: String,
    val comments: String? = ""
)

@Serializable
data class IssueImageModel(
    @Serializable(with = CustomSerializer::class)
    val _id: ObjectId? = null,
    val issueId: @Serializable(with = CustomSerializer::class) ObjectId,
    val imageData: ByteArray,
    val imageName: String? = null,
    val contentType: String? = null
)

@Serializable
enum class IssueStatus {
    ANALISE,
    PENDENTE,
    ANDAMENTO,
    FINALIZADA
}

@Serializable
data class IssueUser(
    val username: String,
    val email: String,
    val userId: String
)

@Serializable
enum class IssueLevel {
    GRAVE,
    MEDIO,
    LEVE
}

@Serializable
data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val address: String? = null
)