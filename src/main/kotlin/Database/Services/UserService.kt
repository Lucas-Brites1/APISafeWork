package com.server.Database.Services

import com.server.Database.Repositories.UserRepositoryImpl
import com.server.Models.Role
import com.server.Models.UserModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes

import com.server.Utils.Hasher

object UserService {
    suspend fun createUser(user: UserModel): Boolean {
        if(UserServiceUtils.validateUser(user)) {
            val userWithHashedPassword = user.copy(password = Hasher.hash(user.password), role = user.role ?: Role.USER)
            UserRepositoryImpl.addUser(user = userWithHashedPassword)
            return true
        } else {
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
