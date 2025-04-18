package com.server.Routes

import com.server.Database.CollectionTypes
import com.server.Database.MongoClientProvider
import com.server.Database.Repositories.IssueRepositoryImpl
import com.server.Database.Services.IssueService
import com.server.Models.IssueModel
import io.ktor.server.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import com.server.Models.UserModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Utils
import io.ktor.http.*
import jdk.jshell.execution.Util
import kotlinx.serialization.json.Json
import org.litote.kmongo.coroutine.toList

internal fun Routing.issueRoutes() {
    route("/problemas") {
        get {
            try {
                val issues : List<IssueModel> = IssueRepositoryImpl.getAllIssues()
                call.respond(HttpStatusCode.OK, Utils.JSONResponse("problemas" to issues))
            }
            catch (e: Exception) {
                Logger.error(method = RouteTypes.GET, message = "Erro ao buscar problemas: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("erro" to "Erro ao buscar problemas"))
            }
        }
    }

    route(path = "/problemas/registro") {
        post {
            try {
                val newIssue = call.receive<IssueModel>() // objeto JSON do body da requisição
                Logger.info(RouteTypes.POST, message = "newIssue: ${newIssue}")
                if(IssueService.createIssue(issue = newIssue)) {
                    call.respond(status = HttpStatusCode.Created, message = Utils.JSONResponse("mensagem" to "Reclamação ${newIssue.title} criada com sucesso!"))
                } else {
                    call.respond(status = HttpStatusCode.BadRequest, message = Utils.JSONResponse("erro" to "Requisição inválida. Verifique os dados enviados e tente novamente."))
                }
            }
            catch (e: Exception) {
                Logger.error(method = RouteTypes.POST, message = "Erro ao tentar registrar nova reclamação. ${e.message}")
                call.respond(HttpStatusCode.InternalServerError)
            }
        }
    }

}