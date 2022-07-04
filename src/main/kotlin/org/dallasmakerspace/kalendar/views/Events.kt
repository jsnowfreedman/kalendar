package org.dallasmakerspace.kalendar.views

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.*
import org.dallasmakerspace.kalendar.generated.Events
import org.dallasmakerspace.kalendar.generated.RoomBooking
import org.dallasmakerspace.kalendar.generated.Rooms
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.registerEventRoutes() {
    routing {
        get("/events") {
            val eventList = transaction {
                addLogger(Slf4jSqlDebugLogger)

                Events.selectAll()
                    .andWhere {
                        Events.eventEnd greaterEq Clock.System.now()
                            .minus(DateTimeUnit.HOUR)
                            .toLocalDateTime(TimeZone.UTC)
                    }
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
                    .toList()
            }

            call.respond(HttpStatusCode.OK, eventList)
        }

        get("/events/create") {
        }

        post("/events/create") {

        }
    }
}