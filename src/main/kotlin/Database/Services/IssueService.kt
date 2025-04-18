package com.server.Database.Services

import com.server.Database.Repositories.IssueRepositoryImpl
import com.server.Models.IssueLevel
import com.server.Models.IssueModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes

object IssueService {
    suspend fun createIssue(issue: IssueModel): Boolean {
        if(IssueServiceUtils.validateIssue(issue = issue)) {
            if(IssueRepositoryImpl.addIssue(issue = issue)) {
                Logger.info(method = RouteTypes.POST, message = "Nova reclamação registrada ${issue.title} criada com sucesso!")
                return true
            }
            Logger.error(method = RouteTypes.POST, message = "Erro ao tentar inserir no banco de dados a nova reclamação.")
            return false
        } else {
            Logger.error(method = RouteTypes.POST, message = "Um erro aconteceu ao tentar inserir uma nova reclamação, informações faltantes ou inválidas.")
            return false
        }
    }
}

object IssueServiceUtils {
    internal fun validateIssue(issue: IssueModel): Boolean {
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
