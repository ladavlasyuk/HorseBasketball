package com.HRGames.HorseBasketball.courtside

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.concurrent.TimeUnit

class CourtsideGateway(private val context: Context) {

    internal val attemptLimits = listOf(15L, 15L, 30L)

    private fun buildClient(timeoutSeconds: Long): OkHttpClient =
        OkHttpClient.Builder()
            .callTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .connectTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .cache(null)
            .build()

    internal suspend fun execute(
        requestUrl: String,
        deadlines: List<Long> = attemptLimits
    ): String? = withContext(Dispatchers.IO) {
        for (deadline in deadlines) {
            val content = runCatching {
                val client = buildClient(deadline)
                val callRequest = Request.Builder()
                    .url(requestUrl)
                    .cacheControl(CacheControl.Builder().noCache().noStore().build())
                    .header("Cache-Control", "no-cache, no-store, must-revalidate")
                    .header("Pragma", "no-cache")
                    .header("Expires", "0")
                    .build()
                client.newCall(callRequest).execute().use { result ->
                    if (result.isSuccessful) result.body?.string() else null
                }
            }.onFailure { error ->
                if (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    Log.w("CourtsideGateway", "Connection attempt failed: ${error.javaClass.simpleName}; cause=${error.cause?.javaClass?.simpleName}")
                }
            }.getOrNull()
            if (!content.isNullOrBlank()) return@withContext content
        }
        null
    }

    suspend fun requestLaunchReply(): String? = execute(composeRequestUrl())

    internal fun composeRequestUrl(): String {
        val profile = HandsetSnapshot(context)
        val parameters = StringBuilder()
            .append("&p=FGHdFHDhgDsgdsGHSD")
            .append("&os=").append(profile.osLabel())
            .append("&lng=").append(profile.language())
            .append("&loc=").append(profile.region())
            .append("&devicemodel=").append(profile.deviceModel())
            .append("&bs=").append(profile.batteryStatus())
            .append("&bl=").append(profile.batteryLevel())
            .append("&nc=").append(profile.getNetworkCountry())
            .append("&sm=").append(profile.getSimState())
            .append("&dv=").append(if (profile.isDeveloperOptionsEnabled()) "1" else "0")
            .toString()
        val token = Base64.encodeToString(parameters.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
        val requestUrl = ENDPOINT.toHttpUrl().newBuilder()
            .addQueryParameter("token", token)
            .build().toString()
        return requestUrl
    }

    companion object {
        const val ENDPOINT = "https://hdgwtri.top/hb-text-info/"
        const val PRIVACY_POLICY_URL = "https://hdgwtri.top/hb-text-info/show.php"
    }
}
