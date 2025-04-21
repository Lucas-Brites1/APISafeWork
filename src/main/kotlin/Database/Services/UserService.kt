package com.server.Database.Services

import com.server.Database.Repositories.UserRepository
import com.server.Models.Role
import com.server.Models.UserModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Hasher

private interface UserService {
    suspend fun login(email: String, password: String): Boolean
    suspend fun registerUser(user: UserModel): Boolean
    suspend fun getUserById(id: String): UserModel?
    suspend fun getUserByEmail(email: String): UserModel?
    suspend fun getAllUsers(): List<UserModel>
}

class UserServiceImpl(private val userRepository: UserRepository, private val userValidator: UserValidator) : UserService{
    override suspend fun login(email: String, password: String): Boolean {
        val user = userRepository.getUserByEmail(email)

        if (user == null) {
            Logger.error(RouteTypes.POST, "Login falhou: usuário com email $email não encontrado.")
            return false
        }

        if (!userValidator.isValid(user)) {
            Logger.error(RouteTypes.POST, "Login falhou: validação de dados do usuário falhou.")
            return false
        }

        if (!Hasher.verify(password = password, hashedPassword = user.password)) {
            Logger.error(RouteTypes.POST, "Login falhou: senha incorreta para o email $email.")
            return false
        }

        Logger.info(RouteTypes.POST, "Login bem-sucedido para o usuário: $email")
        return true
    }

    override suspend fun registerUser(user: UserModel): Boolean {
        if(userValidator.isValid(user)) {
            val userWithHashedPassword = user.copy(password = Hasher.hash(user.password), role = user.role ?: Role.USER)
            if(userRepository.addUser(user = userWithHashedPassword)) {
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

    override suspend fun getUserById(id: String): UserModel? {
        Logger.info(RouteTypes.GET, "Buscando usuário pelo ID: $id")
        return userRepository.findById(id)
    }

    override suspend fun getUserByEmail(email: String): UserModel? {
        Logger.info(RouteTypes.GET, "Buscando usuário pelo e-mail: $email")
        return userRepository.getUserByEmail(email)
    }

    override suspend fun getAllUsers(): List<UserModel> {
        Logger.info(RouteTypes.GET, "Buscando todos os usuários")
        return userRepository.getAllUsers()
    }
}

object UserValidator {
    fun isValid(user: UserModel): Boolean {
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
            return false
        }

        return true
    }
}