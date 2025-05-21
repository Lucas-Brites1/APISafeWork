package com.server.Routes

import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import com.server.Models.UserModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Utils
import io.ktor.http.*
import com.server.Database.Repositories.UserRepositoryImpl
import com.server.Database.Services.*
import com.server.Models.LoginUserModel

internal fun Routing.userRoutes() {
    val userService = UserServiceImpl(userRepository = UserRepositoryImpl(), userValidator = UserValidator)

    route("/usuarios/registro") {
        post {
            try {
                val userRequestBody = call.receive<UserModel>()
                val newUser = userService.registerUser(userRequestBody)

                if (newUser.success) {
                    call.respond(HttpStatusCode.Created, Utils.JSONResponse("mensagem" to newUser.message))
                } else {
                    Logger.error(method = RouteTypes.POST, message = "Erro ao registrar usuário: ${newUser.message}")
                    if (newUser.message.contains("já está em uso", ignoreCase = true)) {
                        call.respond(HttpStatusCode.Conflict, Utils.JSONResponse("erro" to newUser.message))
                    } else {
                        call.respond(HttpStatusCode.BadRequest, Utils.JSONResponse("erro" to newUser.message))
                    }
                }
            } catch (e: Exception) {
                Logger.error(method = RouteTypes.POST, message = "Erro ao processar a requisição de registro: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("erro" to "Erro ao registrar usuário"))
            }
        }
    }

    route("/usuarios/login") {
        post {
            val user = call.receive<LoginUserModel>()
            val userData = userService.getUserByEmail(email = user.email)
            val userLoginValidate = userService.login(email = user.email, password = user.password)

            if (userData != null && userLoginValidate.success) {
                val loginResponse = Utils.LoginResponse(
                    mensagem = userLoginValidate.message,
                    infos = Utils.LoginInfos(
                        userId = userData._id.toString(),
                        email = userData.email,
                        username = userData.username
                    )
                )

                call.respond(HttpStatusCode.OK, loginResponse)
            } else {
                Logger.error(method = RouteTypes.POST, userLoginValidate.message)
                call.respond(HttpStatusCode.BadRequest, Utils.JSONResponse("erro" to userLoginValidate.message))
            }
        }
    }

    route("/usuarios") {
        get {
            try {
                val users: List<UserModel> = userService.getAllUsers()
                call.respond(HttpStatusCode.OK, Utils.JSONResponse("usuários" to users))
            } catch (e: Exception) {
                Logger.error(method = RouteTypes.GET, message = "Erro ao buscar usuários: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("erro" to "Erro ao buscar usuários"))
            }
        }
    }

    route("/usuarios/busca") {
        get {
            val id = call.request.queryParameters["id"]
            if (id != null) {
                val user = userService.getUserById(id = id)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, Utils.JSONResponse("usuário_encontrado" to user))
                } else {
                    call.respond(HttpStatusCode.NotFound, Utils.JSONResponse("erro" to "Usuário não foi encontrado."))
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, Utils.JSONResponse("erro" to "ID inválido ou faltante."))
            }
        }
    }
}