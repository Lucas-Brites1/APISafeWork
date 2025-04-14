package com.server.Routes

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Database.Repositories.UserRepository
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import com.server.Models.UserModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Utils
import io.ktor.http.*
import org.litote.kmongo.coroutine.toList
import com.server.Database.Repositories.UserRepositoryImpl
import com.server.Database.Services.UserService

fun Routing.userRoutes() {
    route("/usuarios/registro") {
        post {
            //email, username, password, role (se não estiver especificada deverá ser ROLE=USER)
            val newUser = call.receive<UserModel>() // objeto JSON do body da requisição

            if (UserService.createUser(newUser)) {
                call.respond(HttpStatusCode.Created, Utils.JSONResponse("mensagem" to "Usuário criado com sucesso: ${newUser.username}"))
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("mensagem" to "Erro ao criar usuário."))
            }
        }
    }

    route("/usuarios") {
        get {
            try {
                val userCollection = MongoClientProvider.getCollection<UserModel>(CollectionTypes.USERS)
                val users = userCollection.find().toList()

                Utils.JSONResponse("usuários" to users)
                call.respond(HttpStatusCode.OK, users)
            } catch (e: Exception) {
                Logger.error(method = RouteTypes.GET, message = "Erro ao buscar usuários: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("mensagem" to "Erro ao buscar usuários"))
            }
        }
    }

    route("/usuarios/busca") {
        get {
            val id = call.request.queryParameters["id"]
            if (id != null) {
                val user = UserRepositoryImpl.findByID(id = id)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, Utils.JSONResponse("usuário_encontrado" to user))
                } else {
                    call.respond(HttpStatusCode.NotFound, Utils.JSONResponse("mensagem" to "Usuário não foi encontrado."))
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, Utils.JSONResponse("mensagem" to "ID inválido ou faltante."))
            }
        }
    }
}