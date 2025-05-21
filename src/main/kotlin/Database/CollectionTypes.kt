package com.server.Database

import com.server.Models.IssueModel
import com.server.Models.UserModel
import kotlin.reflect.KClass
import com.server.Database.MongoClientProvider
import com.server.Models.IssueImageModel

enum class CollectionTypes(val modelClass: KClass<*>, val collectionName: String) {
    USERS(modelClass=UserModel::class, "users"),
    ISSUES(modelClass=IssueModel::class, "issues"),
    IMAGES(modelClass =IssueImageModel::class, collectionName = "images")
}

