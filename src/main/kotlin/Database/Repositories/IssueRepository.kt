package com.server.Database.Repositories

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Models.IssueModel
import com.server.Models.IssueUser
import com.server.Utils.Logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.*

interface IssueRepository {
    suspend fun findByID(id: String): IssueModel?
    suspend fun addIssue(issue: IssueModel): String?
    suspend fun getIssues(length: Int = 0): List<IssueModel>
    suspend fun getIssuesByUserId(userID: String, length: Int): List<IssueModel>
}

class IssueRepositoryImpl : IssueRepository {
    private val issueCollection = MongoClientProvider.getCollection<IssueModel>(collectionType = CollectionTypes.ISSUES)

    override suspend fun findByID(id: String): IssueModel? {
        val objectId = try {
            ObjectId(id)
        } catch (e: IllegalArgumentException) {
            Logger.logException(exception = e, context = "findById")
            return null
        }

        return issueCollection.find(IssueModel::_id eq objectId).awaitFirstOrNull()
    }

    override suspend fun addIssue(issue: IssueModel): String? {
        val newObjectId = ObjectId()
        val issueWithId_ = issue.copy(_id = newObjectId)

        return try {
            val result = issueCollection.insertOne(issueWithId_).awaitFirstOrNull()
            if(result?.wasAcknowledged() == true) {
                newObjectId.toHexString()
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
}