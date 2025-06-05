package com.server.Database.Services

import com.server.Database.Repositories.ImagesRepository
import com.server.Models.IssueImageModel
import com.server.Utils.Logger
import com.server.Utils.RouteTypes

private interface ImageService {
    suspend fun saveImage(image: IssueImageModel): Boolean
    suspend fun getImagesByIssueId(issueId: String): IssueImageModel?
}

class ImageServiceImpl(private val imagesRepository: ImagesRepository) : ImageService {
    override suspend fun saveImage(image: IssueImageModel): Boolean {
        Logger.info(method = RouteTypes.POST, message = "Nova Imagem registrada e vinculada a issue(id): ${image.issueId}!")
        return imagesRepository.addImage(image)
    }

    override suspend fun getImagesByIssueId(issueId: String): IssueImageModel? {
        Logger.info(method = RouteTypes.GET, message = "Buscando imagens vinculadas à issue(id): $issueId")
        return imagesRepository.getImagesByIssueId(issueId)
    }
}