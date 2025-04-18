package com.server.Database

import com.server.Models.IssueModel
import com.server.Models.UserModel
import kotlin.reflect.KClass
import com.server.Database.MongoClientProvider


enum class CollectionTypes(val modelClass: KClass<*>, val collectionName: String) {
    USERS(modelClass=UserModel::class, "users"),
    ISSUES(modelClass=IssueModel::class, "issues")
    // Colocar outras models futuramente
}

