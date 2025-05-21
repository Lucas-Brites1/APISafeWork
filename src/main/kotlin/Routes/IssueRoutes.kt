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

    route(path = "/problemas/busca/{start}{end}") {
        get{
            val start = call.parameters["start"]
            val end = call.parameters["end"]

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
                val newIssue = call.receive<IssueModel>()
                val result = issueService.registerIssue(issue = newIssue)
                if (result.success) {
                    call.respond(status = HttpStatusCode.Created, message = Utils.JSONResponse("id" to result.message))
                } else {
                    call.respond(status = HttpStatusCode.BadRequest, message = Utils.JSONResponse("erro" to result.message))
                }
            } catch (e: ContentTransformationException) {
                Logger.error(method = RouteTypes.POST, message = "Erro ao desserializar a requisição: ${e.message}")
                call.respond(HttpStatusCode.BadRequest, message = Utils.JSONResponse("erro" to "Formato de requisição inválido."))
            } catch (e: Exception) {
                Logger.error(method = RouteTypes.POST, message = "Erro ao tentar registrar nova reclamação: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }
}