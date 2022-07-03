package org.dallasmakerspace.kalendar

import com.google.gson.annotations.SerializedName
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class InstallFile(
    val realm: String,
    @SerializedName("auth-server-url")
    val authServerUrl: String,
    @SerializedName("verify-token-audience")
    val verifyTokenAudience: String,
    val resource: String,
    val credentials: Credentials,
    @SerializedName("provider-name")
    val providerName: String,
) {
    data class Credentials(
        val secret: String,
    )
}

@Serializable
data class OpenidConfiguration(
    val issuer: String,
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String,
    @SerialName("token_endpoint")
    val tokenEndpoint: String,
    @SerialName("introspection_endpoint")
    val introspectionEndpoint: String,
    @SerialName("userinfo_endpoint")
    val userinfoEndpoint: String,
    @SerialName("end_session_endpoint")
    val endSessionEndpoint: String,
    @SerialName("revocation_endpoint")
    val revocationEndpoint: String,
)

data class UserSession(
    var redirect: String? = null,
    var oauthProvider: String? = null,
    var token: String? = null,
    var idHint: String? = null,
) : Principal
