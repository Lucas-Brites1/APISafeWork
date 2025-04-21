package com.server.Routes


import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

import com.server.Utils.Utils

fun Application.configureRouting() {
    routing {
        get("/") {
            Utils.JSONResponse()
            call.respondText("Hello World!")
        }

        userRoutes()
        issueRoutes()
        imageRoutes()
    }
}
