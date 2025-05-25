package com.server.Routes

import com.server.Database.Repositories.ImagesRepositoryImpl
import com.server.Database.Services.ImageServiceImpl
import com.server.Models.IssueImageModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import com.server.Utils.Utils
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import org.bson.types.ObjectId

internal fun Routing.imageRoutes() {
    val imageService = ImageServiceImpl(imagesRepository = ImagesRepositoryImpl())

    route(path = "/problemas/imagens/upload") {
        post {
            val multipartData = call.receiveMultipart()
            var issueId: ObjectId? = null
            var imageData: ByteArray? = null
            var imageName: String? = null
            var contentType: String? = null

            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        if (part.name == "issueId") {
                            issueId = ObjectId(part.value)
                        }
                    }
                    is PartData.FileItem -> {
                        if (part.name == "image") {
                            imageName = part.originalFileName
                            contentType = part.contentType?.toString()
                            val inputProvider = part.provider()
                            imageData = inputProvider.toByteArray()
                        }
                    }
                    else -> part.dispose()
                }
                part.dispose()
            }

            if (issueId == null || !ObjectId.isValid(issueId.toHexString())) {
                call.respond(HttpStatusCode.BadRequest, Utils.JSONResponse("erro" to "ID da Issue inválido ou não fornecido."))
                return@post
            }

            try {

                val isImageSaved = imageService.saveImage(
                    image = IssueImageModel(
                        issueId = issueId, // !! é um null pointer exception se as variaveis forem null vai ser lançado a exceção lá embaixo linha 69
                        imageData = imageData!!,
                        imageName = imageName,
                        contentType = contentType
                    )
                )

                if (isImageSaved) {
                    call.respond(HttpStatusCode.Created, Utils.JSONResponse("mensagem" to "Imagem enviada e associada à Issue com ID: $issueId"))
                } else {
                    call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("erro" to "Erro ao salvar a imagem."))
                }

            } catch (e: Exception) {
                Logger.error(RouteTypes.POST, "Erro ao salvar a imagem: ${e.message}")
                call.respond(HttpStatusCode.InternalServerError, Utils.JSONResponse("erro" to "Erro interno ao salvar a imagem."))
            }
        }
    }
}