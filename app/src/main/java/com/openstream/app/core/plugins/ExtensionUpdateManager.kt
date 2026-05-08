package com.openstream.app.core.plugins

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.openstream.app.core.network.NetworkMonitor
import com.openstream.app.core.notifications.UpdateNotificationManager
import com.openstream.app.data.local.ExtensionDao
import com.openstream.app.data.local.ExtensionEntity
import com.openstream.app.data.local.ExtensionStatus
import com.openstream.app.data.remote.ExtensionApiService
import com.openstream.app.data.remote.RepoConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

private val Context.updateDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "update_settings")

@Singleton
class ExtensionUpdateManager @Inject constructor(
    @ApplicationContext private val context : Context,
    private val dao            : ExtensionDao,
    private val api            : ExtensionApiService,
    private val networkMonitor : NetworkMonitor,
    private val notifManager   : UpdateNotificationManager,
    private val client         : OkHttpClient
) {
    private val tag       = "ExtensionUpdateManager"
    private val AUTO_KEY  = booleanPreferencesKey("auto_update_extensions")

    private fun extDir(): File =
        context.getExternalFilesDir("extensions")
            ?: context.filesDir.resolve("extensions").also { it.mkdirs() }

    // â”€â”€ Public entry point â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    suspend fun checkAndUpdateAll(): UpdateResult = withContext(Dispatchers.IO) {
        // 1. Respect settings toggle
        val prefs   = context.updateDataStore.data.first()
        val autoOn  = prefs[AUTO_KEY] ?: true
        if (!autoOn) { Log.d(tag, "Auto-update disabled, skipping."); return@withContext UpdateResult.NoUpdates }

        // 2. Network check
        if (!networkMonitor.isOnline()) { Log.d(tag, "Offline, skipping update."); return@withContext UpdateResult.Offline }

        // 3. Fetch all repo JSONs in parallel
        val remoteMap = fetchRemoteVersions()
        if (remoteMap.isEmpty()) return@withContext UpdateResult.NoUpdates

        // 4. Compare with installed DB entries
        val installed = dao.getAllInstalled()
        val toUpdate  = installed.filter { entity ->
            val remote = remoteMap[entity.id] ?: return@filter false
            isNewerVersion(remote.version, entity.version)
        }

        if (toUpdate.isEmpty()) return@withContext UpdateResult.NoUpdates

        // 5. Mark as UPDATE_AVAILABLE
        toUpdate.forEach { dao.update(it.copy(updateAvailable = true, status = ExtensionStatus.UPDATE_AVAILABLE)) }

        // 6. Download + hot reload
        val failed    = mutableListOf<String>()
        var succeeded = 0

        toUpdate.forEach { entity ->
            val remote = remoteMap[entity.id] ?: return@forEach
            val result = downloadAndReload(entity, remote.apkUrl, remote.version)
            if (result) succeeded++ else failed.add(entity.name)
        }

        if (succeeded > 0) notifManager.notifyUpdates(succeeded)

        return@withContext when {
            failed.isEmpty()       -> UpdateResult.Success(succeeded)
            succeeded == 0         -> UpdateResult.PartialFailure(failed)
            else                   -> UpdateResult.PartialFailure(failed)
        }
    }

    // â”€â”€ Hot reload â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun hotReloadPlugin(pluginId: String, newPath: String): Boolean {
        return try {
            PluginLoader.unload(pluginId)
            val file = File(newPath)
            val result = PluginLoader.load(context, file.name)
            result is PluginResult.Success
        } catch (e: Exception) {
            Log.e(tag, "Hot reload failed: ${e.message}", e)
            false
        }
    }

    // â”€â”€ Private helpers â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private suspend fun fetchRemoteVersions(): Map<String, RemoteExtInfo> = coroutineScope {
        val results = mutableMapOf<String, RemoteExtInfo>()
        RepoConfig.REPO_URLS.map { url ->
            async(Dispatchers.IO) {
                runCatching {
                    api.fetchRepo(url).forEach { dto ->
                        if (dto.id.isNotBlank()) {
                            results[dto.id] = RemoteExtInfo(dto.version, dto.apkUrl)
                        }
                    }
                }.onFailure { Log.w(tag, "Repo fetch failed: $url â€” ${it.message}") }
            }
        }.forEach { it.await() }
        results
    }

    private suspend fun downloadAndReload(
        entity    : ExtensionEntity,
        apkUrl    : String,
        newVersion: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (apkUrl.isBlank()) return@withContext false
        val fileName = "${entity.name.replace(" ", "_").lowercase()}.cs3"
        val outFile  = File(extDir(), fileName)
        val bakFile  = File(extDir(), "$fileName.bak")

        // Mark as UPDATING
        dao.update(entity.copy(status = ExtensionStatus.UPDATING))

        // Backup old file
        if (outFile.exists()) outFile.copyTo(bakFile, overwrite = true)

        return@withContext try {
            val req  = Request.Builder().url(apkUrl).build()
            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) throw Exception("HTTP ${resp.code}")

            val body = resp.body ?: throw Exception("Empty body")
            FileOutputStream(outFile).use { out ->
                body.byteStream().use { it.copyTo(out) }
            }

            // Hot reload
            val reloaded = hotReloadPlugin(outFile.absolutePath, outFile.absolutePath)
            if (!reloaded) {
                // Rollback
                if (bakFile.exists()) bakFile.copyTo(outFile, overwrite = true)
                dao.update(entity.copy(status = ExtensionStatus.INSTALLED, updateAvailable = true))
                return@withContext false
            }

            // Success â€” update DB
            dao.update(entity.copy(
                version         = newVersion,
                lastUpdated     = System.currentTimeMillis(),
                updateAvailable = false,
                status          = ExtensionStatus.INSTALLED
            ))
            bakFile.delete()
            Log.d(tag, "Updated: ${entity.name} -> $newVersion")
            true

        } catch (e: Exception) {
            Log.e(tag, "Update failed for ${entity.name}: ${e.message}", e)
            // Rollback
            if (bakFile.exists()) bakFile.copyTo(outFile, overwrite = true)
            bakFile.delete()
            dao.update(entity.copy(status = ExtensionStatus.UPDATE_AVAILABLE, updateAvailable = true))
            false
        }
    }

    private fun isNewerVersion(remote: String, local: String): Boolean {
        return try {
            val r = remote.split(".").map { it.trim().toIntOrNull() ?: 0 }
            val l = local.split(".").map  { it.trim().toIntOrNull() ?: 0 }
            val maxLen = maxOf(r.size, l.size)
            for (i in 0 until maxLen) {
                val rv = r.getOrElse(i) { 0 }
                val lv = l.getOrElse(i) { 0 }
                if (rv > lv) return true
                if (rv < lv) return false
            }
            false
        } catch (_: Exception) { false }
    }

    private data class RemoteExtInfo(val version: String, val apkUrl: String)
}
