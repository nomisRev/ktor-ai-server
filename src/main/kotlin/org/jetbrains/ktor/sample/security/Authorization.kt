package org.jetbrains.ktor.sample.security

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.application.isHandled
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.authorized(role: String, vararg anyRole: String, build: Route.() -> Unit) {
    install(RoleBasedAuthorizationPlugin) {
        roles = setOf(role, *anyRole)
    }
    build()
}

private class RoleBasedAuthorizationPluginConfiguration {
    var roles: Set<String> = emptySet()
}

private val RoleBasedAuthorizationPlugin = createRouteScopedPlugin(
    name = "RoleBasedAuthorizationPlugin",
    createConfiguration = ::RoleBasedAuthorizationPluginConfiguration
) {
    val roles = pluginConfig.roles
    if (roles.isEmpty()) return@createRouteScopedPlugin
    else on(AuthenticationChecked) { call ->
        if (call.isHandled) return@on
        val tokenRole = call.principal<UserJWT>()?.user?.role
        val authorized = roles.any { it == tokenRole }
        if (!authorized) {
            val prefix = if (roles.size == 1) "Role " else "Roles "
            val message = roles.joinToString(prefix = prefix, postfix = " required") { it }
            call.respond(HttpStatusCode.Forbidden, message)
        }
    }
}