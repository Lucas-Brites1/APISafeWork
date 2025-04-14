package com.server.Database

import com.server.Models.UserModel
import kotlin.reflect.KClass

enum class CollectionTypes(val modelClass: KClass<*>, val collectionName: String) {
    USERS(UserModel::class, "users")
    // Colocar outras models futuramente
}