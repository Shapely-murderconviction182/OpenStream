package com.openstream.app.core.startup

import android.content.Context
import androidx.startup.Initializer
import com.openstream.app.core.plugins.ExtensionUpdateManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ExtensionUpdateInitializer : Initializer<Unit> {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UpdateManagerEntryPoint {
        fun extensionUpdateManager(): ExtensionUpdateManager
    }

    override fun create(context: Context) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            UpdateManagerEntryPoint::class.java
        )
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching {
                entryPoint.extensionUpdateManager().checkAndUpdateAll()
            }.onFailure {
                android.util.Log.w("UpdateInitializer", "Update check failed: ${it.message}")
            }
        }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
