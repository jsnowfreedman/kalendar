package org.dallasmakerspace.kalendar

import com.google.gson.Gson
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.auth.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path

internal suspend fun getOauthFromInstallFile(
    gson: Gson,
    installFilePath: Path,
): Pair<InstallFile, OpenidConfiguration> {
    val installFile: InstallFile = gson.fromJson(withContext(Dispatchers.IO) {
        Files.readString(installFilePath)
    })

    val urlString = "${installFile.authServerUrl}/realms/${installFile.realm}/.well-known/openid-configuration"
    val oidc: OpenidConfiguration = httpClient.request(urlString).body()

    return installFile to oidc
}

internal fun getOauthServerSettings(
    oauthContext: Pair<InstallFile, OpenidConfiguration>,
    scopes: List<String>,
): OAuthServerSettings.OAuth2ServerSettings {

    val (install, openIdConf) = oauthContext

    return OAuthServerSettings.OAuth2ServerSettings(
        name = install.realm,
        authorizeUrl = openIdConf.authorizationEndpoint,
        accessTokenUrl = openIdConf.tokenEndpoint,
        requestMethod = HttpMethod.Post,
        clientId = install.resource,
        clientSecret = install.credentials.secret,
        defaultScopes = scopes
    )
}

private inline fun <reified T> Gson.fromJson(readString: String?): T {
    return this.fromJson(readString, T::class.java)
}
