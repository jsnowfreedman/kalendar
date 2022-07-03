package org.dallasmakerspace.kalendar.plugins

import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.ldap.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import javax.naming.Context
import javax.naming.directory.SearchControls
import kotlin.collections.MutableMap
import kotlin.collections.iterator
import kotlin.collections.listOf
import kotlin.collections.set
import kotlin.collections.toString

fun Application.configureSecurity() {

    authentication {
        basic("ldapTest") {
            // TODO Make LDAP connector use HOCON and/or pull fromm all the properties to support multiple ldap servers.
            val ldapServer = "prdc1b.dms.local"
            val ldapServerPort = 389
            val bindCn = (System.getProperty("org.dallasmakerspace.kalendar.bind.cn")
                ?: throw Exception("AD Bind Account must be set. (Property: org.dallasmakerspace.kalendar.bind.cn)"))
            val bindPass = (System.getProperty("org.dallasmakerspace.kalendar.bind.password")
                ?: throw Exception("AD Bind Account must be set. (Property: org.dallasmakerspace.kalendar.bind.password)"))

            realm = "dms.local"
            /*
            TODO: Create the binding/admin context as a application local instance.
            When a user tries to auth, we will do a lookup based on their username
            to find their actual CN, and then we will attempt to authenticate to the user to check for auth stuffs.
             */
            validate { credential ->
                ldapAuthenticate(
                    credential,
                    "ldap://$ldapServer:$ldapServerPort",
                    { env: MutableMap<String, Any?> ->
                        env[Context.SECURITY_PRINCIPAL] = bindCn
                        env[Context.SECURITY_CREDENTIALS] = bindPass
                        env[Context.SECURITY_AUTHENTICATION] = "simple"
                    }
                ) { userPasswordCredential ->
                    try {
                        val controls = SearchControls().apply {
                            searchScope = SearchControls.SUBTREE_SCOPE
                            returningAttributes = arrayOf(
                                "sAMAccountName",
                                "distinguishedName",
                                "telephoneNumber",
                                "displayName",
                                "memberOf",
                                "mail"
                            )
                        }

                        this.search(
                            "DC=dms,DC=local",
                            "(& (sAMAccountName=${userPasswordCredential.name}) (objectClass=user))",
                            controls
                        ).asSequence()
                            .firstOrNull {
                                this@configureSecurity.log.info("Attributes:")
                                for (attribute in it.attributes.all) {
                                    this@configureSecurity.log.info(
                                        "\t{}: {}",
                                        attribute.id,
                                        attribute.all.asSequence().joinToString()
                                    )
                                }

                                val ldapPassword = (it.attributes.get("userPassword")?.get() as ByteArray?)?.toString(
                                    Charsets.ISO_8859_1
                                )
                                ldapPassword == credential.password
                            }?.let { UserIdPrincipal(credential.name) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }
        }

        oauth("auth-oauth-google") {
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile")
                )
            }
            client = HttpClient(Apache)
        }
    }

    data class MySession(val count: Int = 0)
    install(Sessions) {
        cookie<MySession>("MY_SESSION") {
            cookie.extensions["SameSite"] = "lax"
        }
    }

    routing {

        authenticate("ldapTest") {
            get("ldaptest") {
                call.respondText {
                    "Hello LDAP!"
                }
            }
        }
        authenticate("auth-oauth-google") {
            get("login") {
                call.respondRedirect("/callback")
            }

            get("/callback") {
                val principal: OAuthAccessTokenResponse.OAuth2? = call.authentication.principal()
                call.sessions.set(UserSession(principal?.accessToken.toString()))
                call.respondRedirect("/hello")
            }
        }
        get("/session/increment") {
            val session = call.sessions.get<MySession>() ?: MySession()
            call.sessions.set(session.copy(count = session.count + 1))
            call.respondText("Counter is ${session.count}. Refresh to increment.")
        }
    }
}

class UserSession(accessToken: String)
