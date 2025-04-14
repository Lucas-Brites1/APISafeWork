package com.server.Database.Repositories

import com.mongodb.reactivestreams.client.MongoCollection
import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Models.UserModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.*

interface UserRepository {
    suspend fun findByID(id: String): UserModel?
    suspend fun addUser(user: UserModel): Boolean
    suspend fun updateUser(id: String, user: UserModel): Boolean
    suspend fun deleteUser(id: String): Boolean
}

object UserRepositoryImpl : UserRepository {
    private val USER_COLLECTION = MongoClientProvider.getCollection<UserModel>(collectionType = CollectionTypes.USERS)

    override suspend fun findByID(id: String): UserModel? {
        val objectId = try {
            ObjectId(id)
        } catch (e: IllegalArgumentException) {
            return null
        }
        return USER_COLLECTION.find(UserModel::_id eq objectId).awaitFirstOrNull()
    }

    override suspend fun addUser(user: UserModel): Boolean {
        return try{
            USER_COLLECTION.insertOne(user).awaitFirstOrNull()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateUser(id: String, user: UserModel): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(id: String): Boolean {
        TODO("Not yet implemented")
    }
}