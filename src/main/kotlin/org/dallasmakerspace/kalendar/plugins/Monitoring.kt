package org.dallasmakerspace.kalendar.plugins

import io.ktor.server.plugins.callloging.*
import org.slf4j.event.*
import io.ktor.server.request.*
import io.ktor.http.*
import io.ktor.server.plugins.callid.*
import io.ktor.server.application.*
import io.ktor.server.response.*

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.DEBUG
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc("call-id")
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        generate(10, "abcde12345")
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }
}
