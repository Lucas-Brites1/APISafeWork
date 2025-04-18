package com.server.Models

import com.server.Utils.CustomSerializer
import kotlinx.serialization.Serializable
import org.bson.types.ObjectId

@Serializable
data class IssueModel(
    @Serializable(with = CustomSerializer::class)
    val _id: ObjectId? = null,
    val user: IssueUser,
    val level: IssueLevel,
    val status: IssueStatus,
    val mapLocal: LocationModel,
    val title: String,
    val description: String,
    val comments: String?,
    val createdAt: Long
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