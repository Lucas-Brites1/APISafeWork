package com.server.Utils
import org.slf4j.LoggerFactory

object Logger {
    private val logger = LoggerFactory.getLogger(Logger::class.java)

    private const val COLOR_INFO = "\u001B[32m"    // Verde
    private const val COLOR_DEBUG = "\u001B[34m"   // Azul
    private const val COLOR_WARN = "\u001B[33m"    // Amarelo
    private const val COLOR_ERROR = "\u001B[31m"   // Vermelho

    private fun formatLogMessage(httpMethod: RouteTypes, message: String, color: String? = null): String {
        val formattedMessage = "[${httpMethod.selectedMethod}] | $message\n"
        return color?.let { "$it$formattedMessage\u001B[0m" } ?: formattedMessage
    }

    fun info(method: RouteTypes, message: String){
        logger.info(formatLogMessage(httpMethod=method, message=message, color = COLOR_INFO))
    }

    fun debug(method: RouteTypes, message: String){
        logger.debug(formatLogMessage(httpMethod=method, message=message, color = COLOR_DEBUG))
    }

    fun warn(method: RouteTypes, message: String){
        logger.warn(formatLogMessage(httpMethod=method, message=message, color = COLOR_WARN))
    }

    fun error(method: RouteTypes, message: String){
        logger.error(formatLogMessage(httpMethod=method, message=message, color = COLOR_ERROR))
    }

    fun exception(method: RouteTypes, message: String, throwable: Throwable){
        val errorMsg = message ?: "Exceção lançada"
        logger.error(formatLogMessage(httpMethod=method, message=errorMsg, color = COLOR_ERROR), throwable)
    }
}

enum class RouteTypes(val selectedMethod: String) {
    GET(selectedMethod = "GET"),
    POST(selectedMethod = "POST"),
    PUT(selectedMethod = "PUT"),
    DELETE(selectedMethod = "DELETE"),
}