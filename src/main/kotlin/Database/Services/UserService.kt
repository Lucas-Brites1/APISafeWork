package com.server.Database.Services

import com.server.Database.Repositories.UserRepositoryImpl
import com.server.Models.Role
import com.server.Models.UserModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes

import com.server.Utils.Hasher

object UserService {

    suspend fun validateLogin(email: String, password: String): Boolean {
        val user = UserRepositoryImpl.getUserByEmail(email)
        if (user != null && UserServiceUtils.validateUser(user)) {
            Logger.info(RouteTypes.POST, message = "Credenciais conferem, login bem sucedido: ${email}")
            return Hasher.verify(password = password, hashedPassword = user.password)
        }

        Logger.error(
            RouteTypes.POST,
            "Não foi possível realizar validação de usuário para o Login, informações faltantes ou inválidas.")
        return false
    }

    suspend fun createUser(user: UserModel): Boolean {
        if(UserServiceUtils.validateUser(user)) {
            val userWithHashedPassword = user.copy(password = Hasher.hash(user.password), role = user.role ?: Role.USER)
            if(UserRepositoryImpl.addUser(user = userWithHashedPassword)) {
                Logger.info(method = RouteTypes.POST, message = "Usúario ${user.username} criado com sucesso!")
                return true
            }
            Logger.error(method = RouteTypes.POST, message = "Erro ao tentar inserir novo usuário no banco de dados.")
            return false
        } else {
            Logger.error(method = RouteTypes.POST, message = "Um erro aconteceu ao tentar inserir novo usuario, informações faltantes ou inválidas.")
            return false
        }
    }
}

object UserServiceUtils {
    internal fun validateUser(user: UserModel): Boolean {
        if (user.email.isBlank()) {
            Logger.error(RouteTypes.POST,"Validação falhou: EMAIL está vazio")
            return false
        }

        if (user.username.isBlank()) {
            Logger.error(RouteTypes.POST,"Validação falhou: USERNAME está vazio")
            return false
        }

        if (user.password.isBlank()) {
            Logger.error(RouteTypes.POST,"Validação falhou: PASSWORD está vazia")
            return false
        }

        if (user.role !in listOf(Role.USER, Role.ADMIN, Role.DEV)) {
            Logger.error(RouteTypes.POST,"Validação falhou: ROLE inválido.")
        }

        return true
    }
}
