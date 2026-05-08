package com.openstream.app.core.update

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppUpdateResult {
    data class UpdateAvailable(val info: AppUpdateInfo) : AppUpdateResult()
    object NoUpdate                                     : AppUpdateResult()
    data class Error(val message: String)               : AppUpdateResult()
}

/**
 * Update check URL â€” point this at your own JSON endpoint.
 * JSON schema: { versionCode, versionName, apkUrl, changelog }
 */
private const val UPDATE_URL = "https://raw.githubusercontent.com/openstream-app/openstream/main/version.json"

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client : OkHttpClient,
    private val json   : Json
) {
    private val tag = "AppUpdateManager"

    suspend fun checkForUpdate(): AppUpdateResult = withContext(Dispatchers.IO) {
        return@withContext try {
            val req      = Request.Builder().url(UPDATE_URL).build()
            val response = client.newCall(req).execute()
            if (!response.isSuccessful) return@withContext AppUpdateResult.Error("HTTP ${response.code}")

            val body = response.body?.string()
                ?: return@withContext AppUpdateResult.Error("Empty response")

            val info         = json.decodeFromString<AppUpdateInfo>(body)
            val currentCode  = context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionCode

            if (info.versionCode > currentCode) {
                Log.d(tag, "Update available: ${info.versionName}")
                AppUpdateResult.UpdateAvailable(info)
            } else {
                Log.d(tag, "No update. Current=$currentCode Remote=${info.versionCode}")
                AppUpdateResult.NoUpdate
            }
        } catch (e: Exception) {
            Log.w(tag, "Update check failed: ${e.message}")
            AppUpdateResult.Error(e.message ?: "Unknown error")
        }
    }
}
