package com.openstream.app.core.plugins

import android.content.Context
import android.util.Log
import dalvik.system.PathClassLoader
import java.io.File

object PluginLoader {

    private const val TAG           = "PluginLoader"
    private const val MAIN_API_NAME = "MainAPI"

    /** Cache: filePath â†’ plugin instance */
    private val cache = HashMap<String, Any>()

    /**
     * Load a .cs3 file from [externalFilesDir]/extensions.
     * Returns [PluginResult.Success] or [PluginResult.Failure] â€” never throws.
     */
    fun load(context: Context, fileName: String): PluginResult {
        return try {
            val dir  = context.getExternalFilesDir("extensions")
                ?: return PluginResult.Failure("External storage unavailable")
            val file = File(dir, fileName)

            if (!file.exists())
                return PluginResult.Failure("File not found: ${file.absolutePath}")
            if (!fileName.endsWith(".cs3"))
                return PluginResult.Failure("Invalid file type: $fileName")

            // Return cached instance if already loaded
            cache[file.absolutePath]?.let { return PluginResult.Success(it) }

            val loader = PathClassLoader(file.absolutePath, context.classLoader)
            val clazz  = findMainApiClass(loader, file)
                ?: return PluginResult.Failure("No MainAPI subclass found in $fileName")

            val instance = clazz.getDeclaredConstructor().newInstance()
            cache[file.absolutePath] = instance
            Log.d(TAG, "Loaded plugin: $fileName -> ${clazz.name}")
            PluginResult.Success(instance)

        } catch (e: ClassNotFoundException)     { fail("Class not found", e) }
          catch (e: NoSuchMethodException)       { fail("No default constructor", e) }
          catch (e: IllegalAccessException)      { fail("Illegal access", e) }
          catch (e: InstantiationException)      { fail("Cannot instantiate", e) }
          catch (e: Exception)                   { fail("Unexpected error", e) }
    }

    /**
     * Remove plugin from cache (does NOT delete the file).
     */
    fun unload(filePath: String) {
        cache.remove(filePath)
        Log.d(TAG, "Unloaded: $filePath")
    }

    fun getCached(filePath: String): Any? = cache[filePath]

    fun allCached(): Map<String, Any> = cache.toMap()

    fun clearAll() { cache.clear() }

    // â”€â”€ Private â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    private fun findMainApiClass(loader: ClassLoader, file: File): Class<*>? {
        return try {
            // Strategy 1: try canonical name "MainAPI" directly
            loader.loadClass(MAIN_API_NAME)
        } catch (_: ClassNotFoundException) {
            // Strategy 2: scan dex entries via DexFile (deprecated but functional for plugins)
            scanDexForMainApi(loader, file)
        }
    }

    @Suppress("DEPRECATION")
    private fun scanDexForMainApi(loader: ClassLoader, file: File): Class<*>? {
        return try {
            val dexFile = dalvik.system.DexFile(file.absolutePath)
            val entries = dexFile.entries()
            while (entries.hasMoreElements()) {
                val name = entries.nextElement()
                runCatching {
                    val clazz = loader.loadClass(name)
                    if (isMainApiSubclass(clazz)) {
                        dexFile.close()
                        return clazz
                    }
                }
            }
            dexFile.close()
            null
        } catch (e: Exception) {
            Log.w(TAG, "Dex scan failed: ${e.message}")
            null
        }
    }

    private fun isMainApiSubclass(clazz: Class<*>): Boolean {
        var superClass: Class<*>? = clazz.superclass
        while (superClass != null) {
            if (superClass.simpleName == MAIN_API_NAME) return true
            superClass = superClass.superclass
        }
        return false
    }

    private fun fail(msg: String, e: Exception): PluginResult.Failure {
        Log.e(TAG, "$msg: ${e.message}", e)
        return PluginResult.Failure("$msg: ${e.message}")
    }
}
