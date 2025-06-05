package com.server.Database.Repositories

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Models.IssueLevel
import com.server.Models.IssueModel
import com.server.Models.IssueStatus
import com.server.Models.IssueUser
import com.server.Models.LocationModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.*
import java.time.LocalDateTime

interface IssueRepository {
    suspend fun addIssue(issue: IssueModel): String?
    suspend fun removeIssue(id: String)
    suspend fun getIssues(length: Int = 0): List<IssueModel>
    suspend fun getIssuesByUserId(userID: String, length: Int): List<IssueModel>
    suspend fun getIssueByIssueId(issueId: String): IssueModel?
    suspend fun getIssuesByTimeRange(start: LocalDateTime, end: LocalDateTime): List<IssueModel>
}

class IssueRepositoryImpl : IssueRepository {
    private val issueCollection = MongoClientProvider.getCollection<IssueModel>(collectionType = CollectionTypes.ISSUES)

    override suspend fun getIssueByIssueId(id: String): IssueModel? {
        val objectId = try {
            ObjectId(id)
        } catch (e: IllegalArgumentException) {
            Logger.logException(exception = e, context = "findById")
            return null
        }

        return issueCollection.find(IssueModel::_id eq objectId).awaitFirstOrNull()
    }

    override suspend fun addIssue(issue: IssueModel): String? {
        val issueWithId_ = issue.copy(_id = issue._id ?: ObjectId())

        return try {
            val result = issueCollection.insertOne(issueWithId_).awaitFirstOrNull()

            if (result?.wasAcknowledged() == true) {
                issueWithId_._id.toHexString()
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "addIssue")
            null
        }
    }

    override suspend fun getIssues(length: Int): List<IssueModel> {
        try {
            val issueCollectionQResult = issueCollection.find().sort(descending(IssueModel::createdAt)).toList()
            return if (length == 0 || length >= issueCollectionQResult.size) {
                issueCollectionQResult
            } else {
                issueCollectionQResult.subList(0, length)
            }
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "getIssues")
            return emptyList()
        }
    }

    override suspend fun getIssuesByUserId(userID: String, length: Int): List<IssueModel> {
        try {
            val issueCollectionQResultFilteredByUserId = issueCollection.find(IssueModel::user / IssueUser::userId eq userID).sort(descending(IssueModel::createdAt)).toList()

            return if (length == 0 || length >= issueCollectionQResultFilteredByUserId.size) {
                issueCollectionQResultFilteredByUserId
            } else {
                issueCollectionQResultFilteredByUserId.subList(0, length)
            }
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "getIssuesByUserId")
            return emptyList()
        }
    }

    override suspend fun getIssuesByTimeRange(start: LocalDateTime, end: LocalDateTime): List<IssueModel> {
        try {
            val filter = and(
                IssueModel::createdAt gte start,
                IssueModel::createdAt lte end
            )
            return issueCollection.find(filter).toList()
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "getIssuesByTimeRange")
            return emptyList()
        }
    }

    override suspend fun removeIssue(id: String) {
        try {
            val issueId = ObjectId(id)

            val deleteResult = issueCollection.deleteOne(IssueModel::_id eq issueId).awaitFirstOrNull()

            if (deleteResult?.deletedCount == 0L) {
                Logger.warn(RouteTypes.DELETE,"Nenhuma reclamação encontrada com o id $id para remover.")
            } else {
                Logger.info(RouteTypes.DELETE,"Reclamação com o id $id removida com sucesso.")
            }
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "removeIssue")
        }
    }
}
