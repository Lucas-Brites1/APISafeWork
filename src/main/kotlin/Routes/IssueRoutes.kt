package com.server.Routes

import com.server.Database.Repositories.IssueRepositoryImpl
import com.server.Database.Services.IssueServiceImpl
import com.server.Database.Services.IssueValidator
import com.server.Models.IssueModel
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Utils
import io.ktor.http.*
import io.ktor.server.util.*
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

internal fun Routing.issueRoutes() {
    val issueService = IssueServiceImpl(IssueRepositoryImpl(), issueValidator = IssueValidator)

    route("/problemas") {
        get {
            val lengthParam = call.parameters.getOrFail("length")
            val length = lengthParam.toIntOrNull() ?: 0

            try {
                val issues : List<IssueModel> = issueService.getAllIssues(length = length)
                call.respond(HttpStatusCode.OK, Utils.JSONResponse("problemas" to issues))
            }
            catch (e: Exception) {
                Logger.error(method = RouteTypes.GET, message = "Erro ao buscar problemas: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("erro" to "Erro ao buscar problemas"))
            }
        }
    }

    route(path = "/problemas/usuarios/{userId}") {
        get {
            val userId = call.parameters.getOrFail("userId")
            val lengthParam = call.parameters["length"]?.toIntOrNull() ?: 0
            val length = lengthParam

            try {
                val issues = issueService.getIssuesByUserId(userId = userId, length = length)
                Logger.info(method = RouteTypes.GET, issues.toString())
                call.respond(HttpStatusCode.OK, issues)
            } catch (e: Exception) {
                Logger.error(
                    method = RouteTypes.GET,
                    message = "Erro ao buscar problemas do usuário $userId: ${e.message}"
                )
                call.respond(
                    HttpStatusCode.InternalServerError,
                    Utils.JSONResponse("erro" to "Erro ao buscar problemas do usuário")
                )
            }
        }
    }

    route(path = "/problemas/busca/limite") {
        get {
            val start = call.request.queryParameters["start"]
            val end = call.request.queryParameters["end"]

            if (start == null || end == null) {
                call.respond(HttpStatusCode.BadRequest, "Parâmetros start e end são obrigatórios.")
                return@get
            }

            try {
                val formatter = DateTimeFormatter.ISO_DATE_TIME
                val startDate = LocalDateTime.parse(start, formatter)
                val endDate = LocalDateTime.parse(end, formatter)

                val issues = issueService.getIssuesByTimeRange(startDate, endDate)
                Logger.info(method = RouteTypes.GET, issues.toString())
                call.respond(HttpStatusCode.OK, issues)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Formato de data inválido. Use: 2025-05-21T00:00:00")
            }
        }
    }

    route(path = "/problemas/registro") {
        post {
            try {
                val rawBody = call.receiveText()
                Logger.info(RouteTypes.POST, "JSON cru recebido: $rawBody")

                val issue = try {
                    Json.decodeFromString(IssueModel.serializer(), rawBody)
                } catch (e: SerializationException) {
                    Logger.error(RouteTypes.POST, "Erro de serialização: ${e.message}")
                    call.respond(HttpStatusCode.BadRequest, Utils.JSONResponse("erro" to "JSON mal formatado: ${e.message}"))
                    return@post
                }

                Logger.info(RouteTypes.POST, "Issue desserializado: $issue")

                val updatedIssue = issue.copy(createdAt = issue.createdAt ?: LocalDateTime.now())

                val result = issueService.registerIssue(issue = updatedIssue)
                if (result.success) {
                    call.respond(status = HttpStatusCode.Created, message = Utils.JSONResponse("id" to result.message))
                } else {
                    call.respond(status = HttpStatusCode.BadRequest, message = Utils.JSONResponse("erro" to result.message))
                }
            } catch (e: Exception) {
                Logger.error(method = RouteTypes.POST, message = "Erro inesperado ao registrar reclamação: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

    route("/problemas/{id}") {
        delete {
            val issueId = call.parameters["id"]

            if (issueId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, Utils.JSONResponse("erro" to "ID da reclamação é obrigatório."))
                return@delete
            }

            try {
                val response = issueService.deleteIssue(issueId)
                Logger.info(method = RouteTypes.DELETE, message = response.message)
                call.respond(HttpStatusCode.OK, Utils.JSONResponse("mensagem" to response.message))
            } catch (e: Exception) {
                Logger.error(method = RouteTypes.DELETE, message = "Erro ao deletar reclamação: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("erro" to "Erro ao deletar reclamação."))
            }
        }
    }
}