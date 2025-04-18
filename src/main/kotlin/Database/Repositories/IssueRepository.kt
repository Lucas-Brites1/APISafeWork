package com.server.Database.Repositories

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Models.IssueModel
import com.server.Models.IssueUser
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.toList

interface IssueRepository {
    suspend fun findByID(id: String): IssueModel?
    suspend fun addIssue(issue: IssueModel): Boolean
    suspend fun getIssueByUserID(userID: String): IssueModel?
    suspend fun getAllIssues(): List<IssueModel>
    suspend fun getAllIssuesByUserID(userID: String): List<IssueModel>
    suspend fun getAllIssuesByUsername(username: String): List<IssueModel>
}

object IssueRepositoryImpl : IssueRepository {
    private val ISSUE_COLLECTION = MongoClientProvider.getCollection<IssueModel>(collectionType = CollectionTypes.ISSUES)

    override suspend fun findByID(id: String): IssueModel? {
        val objectId = try {
            ObjectId(id)
        } catch (e: IllegalArgumentException) {
            return null
        }

        return ISSUE_COLLECTION.find(IssueModel::_id eq objectId).awaitFirstOrNull()
    }

    override suspend fun addIssue(issue: IssueModel): Boolean {
        return try{
            val result = ISSUE_COLLECTION.insertOne(issue).awaitFirstOrNull()
            return result?.wasAcknowledged() == true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getAllIssues(): List<IssueModel> {
        return ISSUE_COLLECTION.find().toList()
    }

    override suspend fun getIssueByUserID(userID: String): IssueModel? {
        TODO("Not yet implemented")
    }

    override suspend fun getAllIssuesByUserID(userID: String): List<IssueModel> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllIssuesByUsername(username: String): List<IssueModel> {
        TODO("Not yet implemented")
    }
}