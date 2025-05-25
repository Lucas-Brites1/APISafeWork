package com.server.Database.Services

import com.server.Database.Repositories.UserRepository
import com.server.Models.Role
import com.server.Models.UserModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Hasher
import com.server.Utils.Utils.ApiResponse

private interface UserService {
    suspend fun login(email: String, password: String): ApiResponse
    suspend fun registerUser(user: UserModel): ApiResponse
    suspend fun getUserById(id: String): UserModel?
    suspend fun getUserByEmail(email: String): UserModel?
    suspend fun getUserRoleById(id: String): Role?
    suspend fun getAllUsers(): List<UserModel>
}

class UserServiceImpl(
    private val userRepository: UserRepository,
    private val userValidator: UserValidator
) : UserService {

    override suspend fun login(email: String, password: String): ApiResponse {
        val user = userRepository.getUserByEmail(email)

        if (user == null) {
            Logger.error(RouteTypes.POST, "Login falhou: usuário com email $email não encontrado.")
            return ApiResponse(
                 success = false,
                 message = "Email não foi encontrado."
            )
        }

        val validationResponse = userValidator.isValid(user)
        if (!validationResponse.success) {
            Logger.error(RouteTypes.POST, "Login falhou: ${validationResponse.message}")
            return ApiResponse(
                success = false,
                message = validationResponse.message
            )
        }

        if (!Hasher.verify(password = password, hashedPassword = user.password)) {
            Logger.error(RouteTypes.POST, "Login falhou: senha incorreta para o email $email.")
            return ApiResponse(
                success = false,
                message = validationResponse.message
            )
        }

        Logger.info(RouteTypes.POST, "Login bem-sucedido para o usuário: $email")
        return ApiResponse(
            success = true,
            message = "Login bem-sucedido"
        )
    }

    override suspend fun registerUser(user: UserModel): ApiResponse {
        val validationResponse = userValidator.isValid(user)
        val emailAlreadyUsing = userRepository.getUserByEmail(email = user.email)

        if (emailAlreadyUsing != null) {
            Logger.error(RouteTypes.POST, "Registro falhou: e-mail ${user.email} já está em uso.")
            return ApiResponse(
                success = false,
                message = "E-mail já está em uso."
            )
        }

        if (validationResponse.success) {
            val userWithHashedPassword = user.copy(password = Hasher.hash(user.password), role = user.role ?: Role.USER)
            if (userRepository.addUser(user = userWithHashedPassword)) {
                Logger.info(method = RouteTypes.POST, message = "Usuário ${user.username} criado com sucesso!")
                return ApiResponse(
                    success = true,
                    message = "Usuário ${user.username} criado com sucesso!"
                )
            }
            Logger.error(method = RouteTypes.POST, message = "Erro ao tentar inserir novo usuário no banco de dados.")
            return ApiResponse(
                success = false,
                message = validationResponse.message
            )
        } else {
            Logger.error(method = RouteTypes.POST, message = "Erro ao tentar inserir novo usuário: ${validationResponse.message}")
            return ApiResponse(
                success = false,
                message = validationResponse.message
            )
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

    override suspend fun getUserRoleById(id: String): Role? {
        Logger.info(method = RouteTypes.GET, "Buscando Role do usuario = {id: $id}")
        return userRepository.getUserRoleById(id = id)
    }
}

object UserValidator {
    fun isValid(user: UserModel): ApiResponse {
        return when {
            user.email.isBlank() -> {
                logValidationError("Validação falhou: EMAIL está vazio")
                ApiResponse(success = false, message = "Validação falhou: EMAIL está vazio")
            }
            !user.email.contains("@") || !user.email.contains(".") -> {
                logValidationError("Validação falhou: EMAIL deve conter '@' e '.'")
                ApiResponse(success = false, message = "Validação falhou: EMAIL deve conter '@' e '.'")
            }
            user.username.isBlank() -> {
                logValidationError("Validação falhou: USERNAME está vazio")
                ApiResponse(success = false, message = "Validação falhou: USERNAME está vazio")
            }
            user.password.isBlank() -> {
                logValidationError("Validação falhou: PASSWORD está vazia")
                ApiResponse(success = false, message = "Validação falhou: PASSWORD está vazia")
            }
            user.password.length < 6 -> {
                logValidationError("Validação falhou: PASSWORD deve ter pelo menos 6 caracteres")
                ApiResponse(success = false, message = "Validação falhou: PASSWORD deve ter pelo menos 6 caracteres")
            }
            user.role !in listOf(Role.USER, Role.ADMIN, Role.DEV) -> {
                logValidationError("Validação falhou: ROLE inválido.")
                ApiResponse(success = false, message = "Validação falhou: ROLE inválido.")
            }
            else -> ApiResponse(success = true, message = "Usuário válido.")
        }
    }

    private fun logValidationError(message: String) {
        Logger.error(RouteTypes.POST, message)
    }
}
