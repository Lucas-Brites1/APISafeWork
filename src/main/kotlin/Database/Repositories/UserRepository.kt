package com.server.Database.Repositories

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Models.UserModel
import com.server.Utils.Logger
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.toList

interface UserRepository {
    suspend fun findById(id: String): UserModel?
    suspend fun addUser(user: UserModel): Boolean
    suspend fun getUserByEmail(email: String): UserModel?
    suspend fun getAllUsers(): List<UserModel>
}

class UserRepositoryImpl : UserRepository {
    private val userCollection = MongoClientProvider.getCollection<UserModel>(collectionType = CollectionTypes.USERS)

    override suspend fun findById(id: String): UserModel? {
        val objectId = try {
            ObjectId(id)
        } catch (e: IllegalArgumentException) {
            Logger.logException(exception = e, context = "findById")
            return null
        }
        return userCollection.find(UserModel::_id eq objectId).awaitFirstOrNull()
    }

    override suspend fun addUser(user: UserModel): Boolean {
        return try{
            val result = userCollection.insertOne(user).awaitFirstOrNull()
            return result?.wasAcknowledged() == true
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "addUser")
            false
        }
    }

    override suspend fun getUserByEmail(email: String): UserModel? {
        return try {
            userCollection.find(UserModel::email eq email).awaitFirstOrNull()
        } catch (e: Exception) {
            Logger.logException(exception = e, context = "getUserByEmail")
            null
        }
    }

    override suspend fun getAllUsers(): List<UserModel> {
        return userCollection.find().toList()
    }
}