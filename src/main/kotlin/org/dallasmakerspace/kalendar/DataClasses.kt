package org.dallasmakerspace.kalendar

import com.google.gson.annotations.SerializedName
import io.ktor.server.auth.*

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

data class OpenidConfiguration(
    val issuer: String,
    @SerializedName("authorization_endpoint")
    val authorizationEndpoint: String,
    @SerializedName("token_endpoint")
    val tokenEndpoint: String,
    @SerializedName("introspection_endpoint")
    val introspectionEndpoint: String,
    @SerializedName("userinfo_endpoint")
    val userinfoEndpoint: String,
    @SerializedName("end_session_endpoint")
    val endSessionEndpoint: String,
    @SerializedName("revocation_endpoint")
    val revocationEndpoint: String,
)

data class UserSession(
    var redirect: String? = null,
    var oauthProvider: String? = null,
    var token: String? = null,
    var idHint: String? = null,
) : Principal

data class UserInfo(
    var sub: String? = null,
    var name: String? = null,
    var preferred_username: String? = null,
    var given_name: String? = null,
    var family_name: String? = null,
    var email: String? = null,
    var groups: List<String>? = null,
)