package org.dallasmakerspace.kalendar.views

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.dallasmakerspace.kalendar.generated.*
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.registerRoomRoutes() {
    routing {
        get("/rooms") {

            val eventList = transaction {
                addLogger(Slf4jSqlDebugLogger)

                (Rooms).selectAll()
                    .map {
                        mapOf(
                            "id" to it[Rooms.id],
                            "name" to it[Rooms.name],
                            "location" to it[Rooms.location],
                            "capacity" to it[Rooms.capacity],
                            "limitations" to it[Rooms.limitations],
                        )
                    }
            }
        }
    }
}