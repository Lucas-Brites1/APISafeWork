package com.server.Database.Repositories

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Models.UserModel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.toList

interface UserRepository {
    suspend fun findByID(id: String): UserModel?
    suspend fun addUser(user: UserModel): Boolean
    suspend fun getUserByEmail(email: String): UserModel?
    suspend fun getAllUsers(): List<UserModel>
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
            val result = USER_COLLECTION.insertOne(user).awaitFirstOrNull()
            return result?.wasAcknowledged() == true
        } catch (e: Exception) {
            false
        }
    }


    override suspend fun getUserByEmail(email: String): UserModel? {
        return USER_COLLECTION.find(UserModel::email eq email).awaitFirstOrNull()
    }

    override suspend fun getAllUsers(): List<UserModel> {
        return USER_COLLECTION.find().toList()
    }
}