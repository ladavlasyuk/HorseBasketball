package com.HRGames.HorseBasketball.courtside

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener

internal data class WebTicket(val url: String, val credential: String? = null)

internal sealed interface LaunchRoute {
    data class Web(val address: String) : LaunchRoute
    data object Game : LaunchRoute
    data object Retry : LaunchRoute
}

internal suspend fun resolveLaunch(preferences: LaunchPreferences, request: suspend () -> String?): LaunchRoute {
    preferences.storedAddress()?.let { return LaunchRoute.Web(it) }
    val reply = request()
    if (reply.isNullOrBlank()) return LaunchRoute.Retry
    val ticket = parseWebTicket(reply) ?: return LaunchRoute.Game
    preferences.rememberTicket(ticket)
    return LaunchRoute.Web(ticket.url)
}

internal fun parseWebTicket(reply: String): WebTicket? {
    val body = reply.trimStart()
    if (body.startsWith("{") || body.startsWith("[")) {
        val stream = JSONTokener(body)
        try {
            while (stream.nextClean() != '\u0000') {
                stream.back()
                locateAddress(stream.nextValue())?.let { return WebTicket(it) }
            }
        } catch (_: JSONException) {
            return null
        }
        return null
    }

    val parts = reply.split('#', limit = 2)
    if (parts.size != 2 || parts.any { it.isBlank() }) return null
    return WebTicket(parts[1], parts[0])
}

private fun locateAddress(value: Any?): String? {
    when (value) {
        is JSONObject -> {
            val candidate = (value.opt("rdstr") as? String)?.trim()?.replaceFirst(Regex("""^(https?)\\+://"""), "$1://")
            if (candidate != null && candidate.toHttpUrlOrNull() != null) return candidate
            for (key in value.keys()) {
                locateAddress(value.opt(key))?.let { return it }
            }
        }
        is JSONArray -> for (index in 0 until value.length()) {
            locateAddress(value.opt(index))?.let { return it }
        }
    }
    return null
}
