package com.openstream.app.core.plugins

import android.content.Context
import android.util.Log
import com.openstream.app.data.local.ExtensionDao
import com.openstream.app.data.local.ExtensionEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadState {
    object Idle                          : DownloadState()
    data class Progress(val percent: Int): DownloadState()
    data class Done(val filePath: String): DownloadState()
    data class Failed(val error: String) : DownloadState()
}

@Singleton
class ExtensionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao    : ExtensionDao,
    private val client : OkHttpClient
) {
    private val tag = "ExtensionManager"

    private fun extDir(): File =
        context.getExternalFilesDir("extensions")
            ?: context.filesDir.resolve("extensions").also { it.mkdirs() }

    // â”€â”€ Download â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun downloadExtension(apkUrl: String, name: String): Flow<DownloadState> = flow {
        emit(DownloadState.Idle)
        if (apkUrl.isBlank()) {
            emit(DownloadState.Failed("Empty URL"))
            return@flow
        }
        val fileName = "${name.replace(" ", "_").lowercase()}.cs3"
        val outFile  = File(extDir(), fileName)

        try {
            val request  = Request.Builder().url(apkUrl).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                emit(DownloadState.Failed("HTTP ${response.code}"))
                return@flow
            }
            val body   = response.body ?: run { emit(DownloadState.Failed("Empty body")); return@flow }
            val total  = body.contentLength()
            var downloaded = 0L

            FileOutputStream(outFile).use { out ->
                body.byteStream().use { src ->
                    val buf = ByteArray(8 * 1024)
                    var n: Int
                    while (src.read(buf).also { n = it } != -1) {
                        out.write(buf, 0, n)
                        downloaded += n
                        if (total > 0) {
                            emit(DownloadState.Progress(((downloaded * 100) / total).toInt()))
                        }
                    }
                }
            }

            // Load plugin
            val result = PluginLoader.load(context, fileName)
            if (result is PluginResult.Failure) {
                Log.w(tag, "Downloaded but load failed: ${result.error}")
            }

            // Update DB
            val existing = withContext(Dispatchers.IO) { dao.findById(name) }
            existing?.let {
                dao.update(it.copy(isInstalled = true, localPath = outFile.absolutePath))
            }

            emit(DownloadState.Done(outFile.absolutePath))

        } catch (e: Exception) {
            Log.e(tag, "Download failed: ${e.message}", e)
            emit(DownloadState.Failed(e.message ?: "Unknown error"))
        }
    }.flowOn(Dispatchers.IO)

    // â”€â”€ Load all installed â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    suspend fun loadAllInstalledExtensions() = withContext(Dispatchers.IO) {
        dao.getAllInstalled().forEach { entity ->
            val path = entity.localPath ?: return@forEach
            val file = File(path)
            if (!file.exists()) {
                Log.w(tag, "File missing, skipping: $path")
                return@forEach
            }
            when (val r = PluginLoader.load(context, file.name)) {
                is PluginResult.Failure -> Log.w(tag, "Load failed: ${r.error}")
                is PluginResult.Success -> Log.d(tag, "Loaded: ${entity.name}")
            }
        }
    }

    // â”€â”€ Unload â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    suspend fun unloadExtension(pluginId: String) = withContext(Dispatchers.IO) {
        val entity = dao.findById(pluginId) ?: return@withContext
        entity.localPath?.let { PluginLoader.unload(it) }
        dao.update(entity.copy(isInstalled = false))
    }

    // â”€â”€ Query â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun getLoadedPlugins(): Map<String, MainAPIBridge> =
        PluginLoader.allCached().mapValues { MainAPIBridge(it.value) }
}
