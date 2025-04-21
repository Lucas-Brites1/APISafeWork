package com.server.Database.Services

import com.server.Database.Repositories.IssueRepository
import com.server.Models.IssueLevel
import com.server.Models.IssueModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes

private interface IssueService {
    suspend fun registerIssue(issue: IssueModel): String?
    suspend fun getIssuesByUserId(userId: String, length: Int): List<IssueModel>
    suspend fun getAllIssues(length: Int): List<IssueModel>
}

class IssueServiceImpl(private val issuesRepository: IssueRepository, private val issueValidator: IssueValidator) : IssueService {
    override suspend fun registerIssue(issue: IssueModel): String? {
        if (issueValidator.isValid(issue = issue)) {
            val result = issuesRepository.addIssue(issue = issue)
            if (!result.isNullOrBlank()) {
                Logger.info(method = RouteTypes.POST, message = "Nova reclamação registrada ${issue.title} criada com sucesso! ID: $result")
                return result
            } else {
                Logger.error(method = RouteTypes.POST, message = "Erro ao tentar inserir no banco de dados a nova reclamação.")
                return null
            }
        } else {
            Logger.error(method = RouteTypes.POST, message = "Um erro aconteceu ao tentar inserir uma nova reclamação, informações faltantes ou inválidas.")
            return null
        }
    }

    override suspend fun getIssuesByUserId(userId: String, length: Int): List<IssueModel> {
        Logger.info(method = RouteTypes.GET, message = "Buscando issues do usuário com id: $userId")

        val issues = issuesRepository.getIssuesByUserId(userId, length)

        return issues
    }

    override suspend fun getAllIssues(length: Int): List<IssueModel> {
        Logger.info(method = RouteTypes.GET, message = "Buscando todas as issues (limit: $length)")

        return issuesRepository.getIssues(length)
    }
}

object IssueValidator {
    internal fun isValid(issue: IssueModel): Boolean {
        if (issue.user.username.isBlank() or issue.user.email.isBlank() or issue.user.userId.isBlank()) {
            Logger.error(RouteTypes.POST,"Validação falhou, informações de usúario faltantes.")
            return false
        }

        if (issue.level !in listOf(IssueLevel.LEVE, IssueLevel.MEDIO, IssueLevel.GRAVE)) {
            Logger.error(RouteTypes.POST,"Validação falhou: LEVEL da reclamação está vazio ou incorreto.")
            return false
        }

        if (issue.title.isBlank()) {
            Logger.error(RouteTypes.POST,"Validação falhou: TITLE está vazia")
            return false
        }

        if (issue.description.isBlank()) {
            Logger.error(RouteTypes.POST,"Validação falhou: DESCRIÇÃO está vazia.")
            return false
        }

        if (issue.mapLocal.longitude == 0.0 || issue.mapLocal.latitude == 0.0) {
            Logger.error(RouteTypes.POST, "Validação falhou: COORDENADAS inválidas.")
            return false
        }

        return true
    }
}
