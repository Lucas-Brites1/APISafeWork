package com.server.Database.Repositories

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Models.IssueImageModel
import com.server.Utils.Logger
import kotlinx.coroutines.reactive.awaitFirstOrNull // ESTE É O IMPORT E MÉTODO CRUCIAL!
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.toList
import org.litote.kmongo.*

interface ImagesRepository {
    suspend fun addImage(image: IssueImageModel): Boolean
    suspend fun getImagesByIssueId(issueId: String): IssueImageModel?
}

class ImagesRepositoryImpl : ImagesRepository {
    private val imageCollection = MongoClientProvider.getCollection<IssueImageModel>(collectionType = CollectionTypes.IMAGES)

    override suspend fun getImagesByIssueId(issueId: String): IssueImageModel? {
        return try {
            val objectId = ObjectId(issueId)
            val resp = imageCollection.find(IssueImageModel::issueId eq objectId).awaitFirstOrNull()
            return resp
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "getImagesByIssueId")
            null
        }
    }

    override suspend fun addImage(image: IssueImageModel): Boolean {
        try {
            val result = imageCollection.insertOne(image).awaitFirstOrNull()
            return result?.wasAcknowledged() == true
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "addImage")
            return false
        }
    }
}