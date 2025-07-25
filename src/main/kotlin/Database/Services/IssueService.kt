package com.server.Database.Services

import com.server.Database.Repositories.IssueRepository
import com.server.Database.Repositories.IssueRepositoryImpl
import com.server.Models.IssueLevel
import com.server.Models.IssueModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Utils.ApiResponse
import io.ktor.server.routing.Route
import org.bson.types.ObjectId
import java.time.LocalDateTime

private interface IssueService {
    suspend fun registerIssue(issue: IssueModel): ApiResponse
    suspend fun getIssuesByUserId(userId: String, length: Int): List<IssueModel>
    suspend fun getAllIssues(length: Int): List<IssueModel>
    suspend fun getIssuesByTimeRange(start: LocalDateTime, end: LocalDateTime): List<IssueModel>
    suspend fun deleteIssue(issueId: String): ApiResponse
    suspend fun getIssueById(issueId: String): IssueModel?
}

class IssueServiceImpl(
    private val issuesRepository: IssueRepository,
    private val issueValidator: IssueValidator
) : IssueService {

    override suspend fun registerIssue(issue: IssueModel): ApiResponse {
        val validationResponse = issueValidator.isValid(issue)

        return if (validationResponse.success) {
            val finalIssue = if (issue._id == null) {
                issue.copy(_id = ObjectId())
            } else {
                issue
            }

            val result = issuesRepository.addIssue(issue = finalIssue)

            val message = if (!result.isNullOrBlank()) {
                "Nova reclamação registrada '${issue.title}' criada com sucesso! ID: $result"
            } else {
                "Erro ao tentar inserir no banco de dados a nova reclamação."
            }

            logResult(message, result != null)

            ApiResponse(success = result != null, message = message)
        } else {
            logResult(validationResponse.message ?: "Erro desconhecido", false)
            ApiResponse(success = false, message = validationResponse.message ?: "Erro desconhecido")
        }
    }

    override suspend fun getIssuesByUserId(userId: String, length: Int): List<IssueModel> {
        Logger.info(method = RouteTypes.GET, message = "Buscando issues do usuário com id: $userId")
        return issuesRepository.getIssuesByUserId(userId, length)
    }

    override suspend fun getIssueById(issueId: String): IssueModel? {
        Logger.info(method = RouteTypes.GET, message = "Buscando issue por ID: $issueId")
        return issuesRepository.getIssueByIssueId(issueId = issueId)
    }

    override suspend fun getAllIssues(length: Int): List<IssueModel> {
        Logger.info(method = RouteTypes.GET, message = "Buscando todas as issues (limit: $length)")
        return issuesRepository.getIssues(length)
    }

    override suspend fun getIssuesByTimeRange(start: LocalDateTime, end: LocalDateTime): List<IssueModel> {
        Logger.info(method = RouteTypes.GET, message = "Buscando todas as issues por intervalo de tempo. (s: $start, d: $end)")
        return issuesRepository.getIssuesByTimeRange(start, end)
    }

    override suspend fun deleteIssue(issueId: String): ApiResponse {
        issuesRepository.removeIssue(issueId)
        return ApiResponse(success = true, message = "Issue: {id: $issueId}, deletada com sucesso!")
    }

    private fun logResult(message: String, success: Boolean) {
        if (success) {
            Logger.info(method = RouteTypes.POST, message = message)
        } else {
            Logger.error(method = RouteTypes.POST, message = message)
        }
    }
}

object IssueValidator {
    internal fun isValid(issue: IssueModel): ApiResponse {
        return when {
            issue.user.username.isBlank() || issue.user.email.isBlank() || issue.user.userId.isBlank() -> {
                logValidationError("Validação falhou, informações de usuário faltantes.")
                ApiResponse(success = false, message = "Validação falhou, informações de usuário faltantes.")
            }
            issue.level !in listOf(IssueLevel.LEVE, IssueLevel.MEDIO, IssueLevel.GRAVE) -> {
                logValidationError("Validação falhou: LEVEL da reclamação está vazio ou incorreto.")
                ApiResponse(success = false, message = "Validação falhou: LEVEL da reclamação está vazio ou incorreto.")
            }
            issue.title.isBlank() -> {
                logValidationError("Validação falhou: TITLE está vazia.")
                ApiResponse(success = false, message = "Validação falhou: TITLE está vazia.")
            }
            issue.description.isBlank() -> {
                logValidationError("Validação falhou: DESCRIÇÃO está vazia.")
                ApiResponse(success = false, message = "Validação falhou: DESCRIÇÃO está vazia.")
            }
            issue.mapLocal.longitude == 0.0 || issue.mapLocal.latitude == 0.0 -> {
                logValidationError("Validação falhou: COORDENADAS inválidas.")
                ApiResponse(success = false, message = "Validação falhou: COORDENADAS inválidas.")
            }
            else -> ApiResponse(success = true, message = "Reclamação válida.")
        }
    }

    private fun logValidationError(message: String) {
        Logger.error(RouteTypes.POST, message)
    }
}
