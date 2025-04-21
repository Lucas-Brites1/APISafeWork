package com.server.Plugins

import com.server.Utils.Logger
import com.server.Utils.RouteTypes
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*

val RouteLoggingPlugin = createRouteScopedPlugin("RouteLoggingPlugin") {
    onCall { call ->
        val method: RouteTypes = RouteTypes.valueOf(call.request.httpMethod.value)
        val path: String = call.request.path()
        Logger.info(method,  "Recebida requisição em $path")
    }

    onCallRespond { call ->
        val method: RouteTypes = RouteTypes.valueOf(call.request.httpMethod.value)?: RouteTypes.GET
        val path: String = call.request.path()
        val status: HttpStatusCode? = call.response.status()

        val commonResponseMessage: String = "Resposta para $path: $status"
        when (status?.value) {
            in 100..399 -> Logger.info(method = method, message = commonResponseMessage)
            in 400..499 -> Logger.warn(method = method, message = commonResponseMessage)
            in 500..599 -> Logger.error(method = method, message = commonResponseMessage)
            else -> Logger.debug(method, "Resposta inesperada para ${path}: ${status}")
        }
    }
}

fun safeRouteType(method: String): RouteTypes? {
    return try {
        RouteTypes.valueOf(method)
    } catch (e: IllegalArgumentException) {
        null
    }
}

fun Application.configureRouteLogging() {
    install(RouteLoggingPlugin)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val method: RouteTypes = safeRouteType(call.request.httpMethod.value) ?: RouteTypes.GET
            val path: String = call.request.path()
            Logger.exception(method = method, message = "Erro inesperado ao processar ${path}", cause)
            call.respond(HttpStatusCode.InternalServerError, "Erro interno do servidor.")
        }
    }
}