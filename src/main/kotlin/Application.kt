package com.server

import com.server.Plugins.configureRouteLogging
import com.server.Routes.configureRouting
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import io.ktor.server.application.*

fun main(args: Array<String>) {
    embeddedServer(Netty, port=8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouteLogging()
    configureRouting()
}


