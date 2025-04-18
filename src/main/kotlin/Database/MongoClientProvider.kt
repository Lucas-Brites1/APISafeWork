package com.server.Database

import com.mongodb.ConnectionString
import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoCollection
import com.server.Utils.Utils
import org.litote.kmongo.reactivestreams.KMongo

object MongoClientProvider {
    private val CONNECTION_STRING: String = Utils.getEnv(key = "DATABASE_CONNECTION_STRING") ?: ""

    private val client = if (CONNECTION_STRING.isNotBlank()) {
        KMongo.createClient(ConnectionString(CONNECTION_STRING))  // Cria o cliente com coroutines
    } else {
        throw IllegalStateException("A variável de ambiente 'DATABASE_CONNECTION_STRING' não foi configurada corretamente.")
    }

    private val database = client.getDatabase("Safework")

    internal inline fun <reified T:Any>getCollection(collectionType: CollectionTypes): MongoCollection<T> {
        return database.getCollection(collectionType.collectionName, T::class.java)
    }

    internal fun getClient(): MongoClient {
        return client
    }
}
