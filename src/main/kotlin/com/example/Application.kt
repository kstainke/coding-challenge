package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.routes.registerSearchRoutes
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.serialization.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }
        registerSearchRoutes()
    }.start(wait = true)
}
