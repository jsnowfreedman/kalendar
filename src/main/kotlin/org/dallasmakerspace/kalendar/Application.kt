package org.dallasmakerspace.kalendar

import com.github.mustachejava.DefaultMustacheFactory
import com.google.gson.GsonBuilder
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.mustache.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.dallasmakerspace.kalendar.views.registerEventRoutes
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import java.nio.file.Files.walk
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.streams.toList

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
}

val redirectUri: String = System.getProperty("kalendar.oauth.redirectUrl", "http://localhost:8080")

fun main(): Unit = runBlocking {

    val config = ConfigFactory.load(System.getProperty("org.dallasmakerspace.config", "config/kalendar.conf"))
    initializeDatabaseConnections(config)
//    val database = Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

    val gson = GsonBuilder().setPrettyPrinting().create()

    val oauthContextsDef = withContext(Dispatchers.IO) {
        walk(Path.of("secrets/oauth")).filter { it.isDirectory().not() }.map {
            async {
                val oauthFromInstallFile = getOauthFromInstallFile(gson, it)
                oauthFromInstallFile.first.providerName to oauthFromInstallFile
            }
        }.toList()
    }

    val oauthContexts = oauthContextsDef.awaitAll().toMap()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(Mustache) {
            mustacheFactory = DefaultMustacheFactory("templates")
        }

        install(Sessions) {
            cookie<UserSession>("user_session")
        }

        installOauthContexts(oauthContexts)

        registerEventRoutes()

        routing {
            get("/") {
                call.respondRedirect("/events")
            }
            get("/events-old") {
                val userSession: UserSession? = call.sessions.get()
                val oauthContext = oauthContexts[userSession?.oauthProvider]
                if (userSession?.token != null && oauthContext != null) {
                    call.respondText(httpClient.get(oauthContext.second.userinfoEndpoint) {
                        headers {
                            append(HttpHeaders.Authorization, "Bearer ${userSession.token}")
                        }
                    }.bodyAsText())
                }
            }
        }
    }.start(wait = true)
}

fun Application.installOauthContexts(oauthContexts: Map<String, Pair<InstallFile, OpenidConfiguration>>) {
    pluginRegistry.getOrNull(Sessions.key) ?: run {
        install(Sessions) {
            cookie<UserSession>("user_session")
        }
    }

    install(Authentication) {
        oauthContexts.forEach { (k, v) ->
            oauth(k) {
                urlProvider = { "$redirectUri/login/callback/$k" }
                providerLookup = { getOauthServerSettings(v, listOf()) }
                client = httpClient
            }
        }
    }

    routing {

        get("/logout") {
            call.sessions.getOrSet { UserSession() }.also {

                if (it.oauthProvider == null || it.oauthProvider !in oauthContexts) {
                    call.sessions.clear<UserSession>()
                    return@also
                }

                val (install, openid) = oauthContexts[it.oauthProvider]!!
                try {
                    val urlString = openid.revocationEndpoint

                    val bodyStr = mapOf(
                        "client_id" to install.resource,
                        "client_secret" to install.credentials.secret,
                        "token" to it.token
                    ).map { (k, v) -> "$k=$v" }.joinToString("&")

                    httpClient.post(urlString) {
                        setBody(bodyStr)
                        contentType(ContentType.parse("application/x-www-form-urlencoded"))
                    }
//                    val http: HttpStatement = httpClient.post(urlString) {
//                        body = bodyStr
//                        contentType(ContentType.parse("application/x-www-form-urlencoded"))
//                    }
//
//                    http.execute() { resp ->
//                        println(resp.status)
//                        println(resp.readText())
//                    }

                    call.sessions.clear<UserSession>()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            call.request.queryParameters["redirect"]?.also {
                call.respondRedirect(it)
            }
        }

        oauthContexts.forEach { (oauthProvider, _) ->
            println("Adding oauthProvider: $oauthProvider")
            get("/login/$oauthProvider") {
                // Redirects to 'authorizeUrl' automatically
                if (call.request.queryParameters.contains("redirect")) {
                    val session = call.sessions.getOrSet { UserSession() }
                    session.redirect = call.request.queryParameters["redirect"]
                    session.oauthProvider = oauthProvider
                }
                call.respondRedirect("/login/redirect/$oauthProvider")
            }

            authenticate(oauthProvider) {
                get("/login/redirect/$oauthProvider") {}
                get("/login/callback/$oauthProvider") {
                    val session = call.sessions.getOrSet { UserSession() }
                    val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    session.oauthProvider = oauthProvider
                    session.token = principal?.accessToken.toString()
                    session.idHint = principal?.extraParameters?.get("id_token")

                    println(principal)

                    val redirect = session.redirect
                    if (redirect != null) {
                        call.respondRedirect(redirect)
                        session.redirect = null
                    } else {
                        call.respondRedirect(redirectUri)
                    }
                }
            }
        }
    }
}

fun initializeDatabaseConnections(appConfig: Config) {
    val dataSource = HikariDataSource(HikariConfig().apply {
        jdbcUrl = appConfig.tryGetString("database.jdbcUrl") ?: "jdbc:postgresql://localhost:5430/kalendar"
        username = appConfig.tryGetString("database.username") ?: "kalendar_default"
        password = appConfig.tryGetString("database.password") ?: "kalendar_default"
    })
    val flyway = Flyway.configure().locations("filesystem:migrations/db").dataSource(dataSource).load()

//    flyway.repair()
    flyway.migrate()

    Database.connect(dataSource)
}