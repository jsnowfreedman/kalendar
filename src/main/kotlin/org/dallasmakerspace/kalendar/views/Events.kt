package org.dallasmakerspace.kalendar.views

import io.ktor.server.application.*
import io.ktor.server.mustache.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import org.dallasmakerspace.kalendar.generated.Equipment
import org.dallasmakerspace.kalendar.generated.Events
import org.dallasmakerspace.kalendar.generated.RoomBooking
import org.dallasmakerspace.kalendar.generated.Rooms
import org.dallasmakerspace.kalendar.plugins.MustacheUser
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.registerEventRoutes() {
    routing {
        get("/events") {
            transaction {
                addLogger(Slf4jSqlDebugLogger)

                Events.selectAll()
                    .andWhere { Events.eventEnd greaterEq Clock.System.now().minus(DateTimeUnit.HOUR) }
                    .andWhere { Events.status eq "approved" }
                    .map {
                        val primaryRoomBooking = (RoomBooking leftJoin Rooms).select {
                            RoomBooking.eventId.eq(it[Events.id].value)
                        }.orderBy(RoomBooking.startTime).single()

                        mapOf(
                            "uuid" to it[Events.id].value,
                            "name" to it[Events.name],
                            "shortDescription" to it[Events.shortDescription],
                            "longDescription" to it[Events.longDescription],
                            "startDate" to it[Events.eventStart],
                            "endDate" to it[Events.eventEnd],
                            "host" to it[Events.creatorId], // TODO create user/contact table which this should join with...
                            "cost" to it[Events.memberCost],
                            "location" to mapOf(
                                "room" to primaryRoomBooking[Rooms.name]
                            )
                        )
                    }
            }

            call.respond(Clock.System.now().toString())
        }

        get("/events/create") {
            call.respond(MustacheContent("event-create.hbs", mapOf("user" to MustacheUser(1, "user1"))))
        }

        post("/events/create") {

        }
    }
}